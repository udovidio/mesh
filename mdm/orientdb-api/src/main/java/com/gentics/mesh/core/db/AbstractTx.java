/**
 * Copyright 2004 - 2016 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gentics.mesh.core.db;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;

import com.gentics.mesh.core.data.HibBaseElement;
import com.gentics.mesh.core.data.HibElement;
import com.gentics.mesh.madl.frame.ElementFrame;
import com.gentics.mesh.util.StreamUtil;
import com.syncleus.ferma.FramedTransactionalGraph;

/**
 * An abstract class that can be used to implement vendor specific graph database Tx classes.
 */
public abstract class AbstractTx<T extends FramedTransactionalGraph> implements GraphDBTx {

	/**
	 * Graph that is active within the scope of the autoclosable.
	 */
	private T currentGraph;

	private boolean isSuccess = false;

	/**
	 * Initialize the transaction.
	 * 
	 * @param transactionalGraph
	 */
	protected void init(T transactionalGraph) {
		// 1. Set the new transactional graph so that it can be accessed via Tx.getGraph()
		setGraph(transactionalGraph);
		// Handle graph multithreading issues by storing the old graph instance that was found in the threadlocal in a field.
		// Overwrite the current active threadlocal graph with the given transactional graph. This way Ferma graph elements will utilize this instance.
		Tx.setActive(this);
	}

	@Override
	public void success() {
		isSuccess = true;
	}

	@Override
	public void failure() {
		isSuccess = false;
	}

	/**
	 * Return the state of the success status flag.
	 * 
	 * @return
	 */
	protected boolean isSuccess() {
		return isSuccess;
	}

	@Override
	public void close() {
		Tx.setActive(null);
		if (isSuccess()) {
			commit();
		} else {
			rollback();
		}
		// Restore the old graph that was previously swapped with the current graph
		getGraph().close();
		getGraph().shutdown();
	}

	/**
	 * Invoke a commit on the database of this transaction.
	 */
	public void commit() {
		if (getGraph() instanceof FramedTransactionalGraph) {
			((FramedTransactionalGraph) getGraph()).commit();
		}
	}

	/**
	 * Invoke a rollback on the database of this transaction.
	 */
	public void rollback() {
		if (getGraph() instanceof FramedTransactionalGraph) {
			((FramedTransactionalGraph) getGraph()).rollback();
		}
	}

	/**
	 * Return the internal graph reference.
	 */
	public FramedTransactionalGraph getGraph() {
		return currentGraph;
	}

	/**
	 * Set the internal graph reference.
	 *
	 * @param currentGraph
	 */
	protected void setGraph(T currentGraph) {
		this.currentGraph = currentGraph;
	}

	@Override
	public <B extends HibElement> B create(String uuid, Class<? extends B> classOfB) {
		B entity = getGraph().addFramedVertex(classOfB);
		if (StringUtils.isNotBlank(uuid)) {
			entity.setUuid(uuid);
			persist(entity);
		}
		return entity;
	}
	
	@Override
	public <B extends HibElement> B persist(B element) {
		/*
		 * Since OrientDB does not tell apart POJOs and persistent entities, 
		 * processing the entity updates directly into the persistent state, 
		 * the merge implementation here is empty.
		 */
		return element;
	}
	
	@Override
	public <B extends HibElement> void delete(B element) {
		((ElementFrame) element).remove();
	}

	@Override
	public <B extends HibElement> long count(Class<? extends B> classOfB) {
		if (HibBaseElement.class.isAssignableFrom(classOfB)) {
			return data().mesh().database().count((Class<? extends HibBaseElement>) classOfB);
		} else {
			return StreamSupport.stream(getGraph().getVertices(ElementFrame.TYPE_RESOLUTION_KEY, classOfB.getName())
					.spliterator(), true).count();
		}
	}

	@Override
	public <B extends HibElement> B load(Object id, Class<? extends B> classOfB) {
		B b = getGraph().getFramedVertexExplicit(classOfB, id);
		try {
			b.getUuid();
		} catch (Throwable e) {
			b = null;
		}
		return b;
	}

	@Override
	public <I extends HibElement, B extends I> Stream<I> loadAll(Class<B> classOfB) {
		return StreamUtil.toStream(getGraph().v().has(classOfB).frameExplicit(classOfB)) ;
	}
}
