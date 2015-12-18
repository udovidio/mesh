//package com.gentics.mesh.core.data.generic;
//
//import com.gentics.mesh.cli.BootstrapInitializer;
//import com.gentics.mesh.core.data.IndexableElement;
//import com.gentics.mesh.core.data.search.SearchQueue;
//import com.gentics.mesh.core.data.search.SearchQueueBatch;
//import com.gentics.mesh.core.data.search.SearchQueueEntryAction;
//import com.gentics.mesh.core.rest.common.RestModel;
//import com.gentics.mesh.util.UUIDUtil;
//
//public abstract class AbstractCoreElement<T extends RestModel> extends AbstractMeshCoreVertex<T>
//		implements IndexableElement {
//
//	@Override
//	public SearchQueueBatch addIndexBatch(SearchQueueEntryAction action) {
//		SearchQueue queue = BootstrapInitializer.getBoot().meshRoot().getSearchQueue();
//		SearchQueueBatch batch = queue.createBatch(UUIDUtil.randomUUID());
//		batch.addEntry(this, action);
//		addRelatedEntries(batch, action);
//		return batch;
//	}
//
//
//}
