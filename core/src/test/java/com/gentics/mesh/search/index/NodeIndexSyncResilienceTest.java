package com.gentics.mesh.search.index;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.test.context.ElasticsearchTestMode.CONTAINER_ES6_TOXIC;
import static com.gentics.mesh.test.context.MeshTestHelper.getSimpleQuery;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.node.NodeListResponse;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.parameter.impl.VersioningParametersImpl;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.SlowClose;

@MeshTestSetting(elasticsearch = CONTAINER_ES6_TOXIC, testSize = TestSize.FULL, startServer = true)
public class NodeIndexSyncResilienceTest extends AbstractMeshTest {

	@Test
	public void testNodeSync() throws Exception {

		recreateIndices();

		String oldContent = "supersonic";
		String newContent = "urschnell";

		// "urschnell" not found in published nodes
		NodeListResponse response = call(() -> client().searchNodes(PROJECT_NAME, getSimpleQuery("fields.content", newContent)));
		assertThat(response.getData()).as("Published search result").isEmpty();

		String uuid = db().tx(() -> content("concorde").getUuid());

		// publish the Concorde
		NodeResponse concorde = call(() -> client().findNodeByUuid(PROJECT_NAME, uuid, new VersioningParametersImpl().draft()));
		call(() -> client().publishNode(PROJECT_NAME, uuid));

		waitForSearchIdleEvent();

		// "supersonic" found in published nodes
		response = call(() -> client().searchNodes(PROJECT_NAME, getSimpleQuery("fields.content", oldContent)));
		assertThat(response.getData()).as("Published search result").usingElementComparatorOnFields("uuid").containsOnly(concorde);

		grantAdmin();

		// Now clear all data
		searchProvider().clear().blockingAwait();

		// Close connection
		SlowClose toxcitiyIn = toxics().slowClose("ES-IN", ToxicDirection.DOWNSTREAM, 0);
		SlowClose toxcitiyOut = toxics().slowClose("ES-OUT", ToxicDirection.UPSTREAM, 0);
		sleep(500);
		// Invoke the sync
		GenericMessageResponse message = call(() -> client().invokeIndexSync());
		assertThat(message).matches("search_admin_index_sync_invoked");

		// Re-enable traffic after 2s
		sleep(4000);
		waitForSearchIdleEvent();
//		toxcitiyOut.remove();
//		toxcitiyIn.remove();

		response = call(() -> client().searchNodes(PROJECT_NAME, getSimpleQuery("fields.content", oldContent)));
		assertThat(response.getData()).as("Published search result").usingElementComparatorOnFields("uuid").containsOnly(concorde);

	}
}
