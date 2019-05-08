package com.gentics.mesh.search;

import com.gentics.mesh.core.data.node.Micronode;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.field.list.MicronodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.list.NodeGraphFieldList;
import com.gentics.mesh.core.data.node.field.nesting.MicronodeGraphField;
import com.gentics.mesh.core.rest.node.NodeListResponse;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.SchemaModel;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.MicronodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.parameter.impl.NodeParametersImpl;
import com.gentics.mesh.parameter.impl.PagingParametersImpl;
import com.gentics.mesh.parameter.impl.VersioningParametersImpl;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.util.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.test.context.MeshTestHelper.getSimpleQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractNodeSearchEndpointTest extends AbstractMeshTest {

	/**
	 * Do the search with the given set of expected languages and assert correctness of the result.
	 *
	 * @param expectedLanguages
	 * @throws InterruptedException
	 * @throws JSONException
	 */
	protected void searchWithLanguages(String... expectedLanguages) throws Exception {
		recreateIndices();

		String uuid = db().tx(() -> content("concorde").getUuid());

		NodeListResponse response = call(
			() -> client().searchNodes(PROJECT_NAME, getSimpleQuery("fields.content", "concorde"),
				new PagingParametersImpl().setPage(1).setPerPage(100L),
				new NodeParametersImpl().setLanguages(expectedLanguages), new VersioningParametersImpl().draft()));
		assertEquals("Check # of returned nodes", expectedLanguages.length, response.getData().size());
		assertEquals("Check total count", expectedLanguages.length, response.getMetainfo().getTotalCount());

		Set<String> foundLanguages = new HashSet<>();
		for (NodeResponse nodeResponse : response.getData()) {
			assertEquals("Check uuid of found node", uuid, nodeResponse.getUuid());
			foundLanguages.add(nodeResponse.getLanguage());
		}

		Set<String> notFound = new HashSet<>(Arrays.asList(expectedLanguages));
		notFound.removeAll(foundLanguages);
		assertTrue("Did not find nodes in expected languages: " + notFound, notFound.isEmpty());

		Set<String> unexpected = new HashSet<>(foundLanguages);
		unexpected.removeAll(Arrays.asList(expectedLanguages));
		assertTrue("Found nodes in unexpected languages: " + unexpected, unexpected.isEmpty());
	}

	protected void addNumberSpeedFieldToOneNode(Number number) {
		Node node = content("concorde");
		SchemaModel schema = node.getSchemaContainer().getLatestVersion().getSchema();
		schema.addField(new NumberFieldSchemaImpl().setName("speed"));
		node.getSchemaContainer().getLatestVersion().setSchema(schema);

		node.getLatestDraftFieldContainer(english()).createNumber("speed").setNumber(number);
	}

	/**
	 * Add a micronode field to the tested content
	 */
	protected void addMicronodeField() {
		Node node = content("concorde");

		Schema schema = node.getSchemaContainer().getLatestVersion().getSchema();
		MicronodeFieldSchemaImpl vcardFieldSchema = new MicronodeFieldSchemaImpl();
		vcardFieldSchema.setName("vcard");
		vcardFieldSchema.setAllowedMicroSchemas(new String[] { "vcard" });
		schema.addField(vcardFieldSchema);

		MicronodeGraphField vcardField = node.getLatestDraftFieldContainer(english()).createMicronode("vcard",
			microschemaContainers().get("vcard").getLatestVersion());
		vcardField.getMicronode().createString("firstName").setString("Mickey");
		vcardField.getMicronode().createString("lastName").setString("Mouse");
	}

	/**
	 * Add a micronode list field to the tested content
	 */
	protected void addMicronodeListField() {
		Node node = content("concorde");

		// Update the schema
		Schema schema = node.getSchemaContainer().getLatestVersion().getSchema();
		ListFieldSchema vcardListFieldSchema = new ListFieldSchemaImpl();
		vcardListFieldSchema.setName("vcardlist");
		vcardListFieldSchema.setListType("micronode");
		vcardListFieldSchema.setAllowedSchemas(new String[] { "vcard" });
		schema.addField(vcardListFieldSchema);

		MicronodeGraphFieldList vcardListField = node.getLatestDraftFieldContainer(english()).createMicronodeFieldList("vcardlist");
		for (Tuple<String, String> testdata : Arrays.asList(Tuple.tuple("Mickey", "Mouse"), Tuple.tuple("Donald", "Duck"))) {
			Micronode micronode = vcardListField.createMicronode();
			micronode.setSchemaContainerVersion(microschemaContainers().get("vcard").getLatestVersion());
			micronode.createString("firstName").setString(testdata.v1());
			micronode.createString("lastName").setString(testdata.v2());
		}

		// create an empty vcard list field
		node.getLatestDraftFieldContainer(german()).createMicronodeFieldList("vcardlist");
	}

	/**
	 * Add a node list field to the tested content
	 */
	protected void addNodeListField() {
		Node node = content("concorde");

		// Update the schema
		Schema schema = node.getSchemaContainer().getLatestVersion().getSchema();
		ListFieldSchema nodeListFieldSchema = new ListFieldSchemaImpl();
		nodeListFieldSchema.setName("nodelist");
		nodeListFieldSchema.setListType("node");
		nodeListFieldSchema.setAllowedSchemas(schema.getName());
		schema.addField(nodeListFieldSchema);

		// create a non-empty list for the english version
		NodeGraphFieldList nodeListField = node.getLatestDraftFieldContainer(english()).createNodeList("nodelist");
		nodeListField.addItem(nodeListField.createNode("testNode", node));

		// create an empty list for the german version
		node.getLatestDraftFieldContainer(german()).createNodeList("nodelist");
	}

	/**
	 * Generate the JSON for a searched in the nested field vcardlist
	 * 
	 * @param firstName
	 *            firstname to search for
	 * @param lastName
	 *            lastname to search for
	 * @return search JSON
	 * @throws IOException
	 */
	protected String getNestedVCardListSearch(String firstName, String lastName) throws IOException {
		JsonObject request = new JsonObject(
			"{\"query\":{\"nested\":{\"path\":\"fields.vcardlist\",\"query\":{\"bool\":{\"must\":[{\"match\":{\"fields.vcardlist.fields-vcard.firstName\":\"Mickey\"}},{\"match\":{\"fields.vcardlist.fields-vcard.lastName\":\"Duck\"}}]}}}}}\n");
		JsonArray must = request.getJsonObject("query").getJsonObject("nested").getJsonObject("query").getJsonObject("bool").getJsonArray("must");
		must.getJsonObject(0).getJsonObject("match").put("fields.vcardlist.fields-vcard.firstName", firstName);
		must.getJsonObject(1).getJsonObject("match").put("fields.vcardlist.fields-vcard.lastName", lastName);
		return request.toString();
	}
}
