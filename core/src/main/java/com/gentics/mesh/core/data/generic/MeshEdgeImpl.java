package com.gentics.mesh.core.data.generic;

import com.gentics.madl.annotations.GraphElement;
import com.gentics.madl.frame.AbstractEdgeFrame;
import com.gentics.madl.tx.Tx;
import com.gentics.mesh.core.data.MeshEdge;
import com.gentics.mesh.core.graph.GraphAttribute;
import com.gentics.mesh.dagger.MeshComponent;
import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.util.UUIDUtil;
import com.syncleus.ferma.FramedGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.wrappers.wrapped.WrappedEdge;
import com.tinkerpop.blueprints.util.wrappers.wrapped.WrappedElement;

import io.vertx.core.Vertx;

/**
 * @see MeshEdge
 */
@GraphElement
public class MeshEdgeImpl extends AbstractEdgeFrame implements MeshEdge {
	
	@Override
	protected void init() {
		super.init();
		setProperty("uuid", UUIDUtil.randomUUID());
	}

	@Override
	protected void init(FramedGraph graph, Element e, Object id) {
		super.init(graph, null, id);
	}

	public String getFermaType() {
		return property(TYPE_RESOLUTION_KEY);
	}

	public String getUuid() {
		return property("uuid");
	}

	public void setUuid(String uuid) {
		property("uuid", uuid);
	}

	@Override
	public FramedGraph getGraph() {
		return Tx.get().getGraph();
	}

	public MeshEdgeImpl getImpl() {
		return this;
	}

	@Override
	public String getElementVersion() {
		Edge edge = getElement();
		return db().getElementVersion(edge);
	}

	public MeshComponent mesh() {
		return getGraphAttribute(GraphAttribute.MESH_COMPONENT);
	}

	@Override
	public Database db() {
		return mesh().database();
	}

	@Override
	public Vertx vertx() {
		return mesh().vertx();
	}

	@Override
	public MeshOptions options() {
		return mesh().options();
	}

}
