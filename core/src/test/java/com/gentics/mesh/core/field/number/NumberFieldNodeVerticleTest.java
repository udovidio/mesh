package com.gentics.mesh.core.field.number;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import io.vertx.core.Future;

import java.io.IOException;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

import com.gentics.mesh.core.data.NodeFieldContainer;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.node.field.basic.NumberField;
import com.gentics.mesh.core.field.AbstractFieldNodeVerticleTest;
import com.gentics.mesh.core.rest.node.NodeRequestParameters;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.schema.NumberFieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.demo.DemoDataProvider;

public class NumberFieldNodeVerticleTest extends AbstractFieldNodeVerticleTest {

	@Before
	public void updateSchema() throws IOException {
		Schema schema = schemaContainer("folder").getSchema();
		NumberFieldSchema numberFieldSchema = new NumberFieldSchemaImpl();
		numberFieldSchema.setName("numberField");
		numberFieldSchema.setMin(10);
		numberFieldSchema.setMax(1000);
		numberFieldSchema.setRequired(true);
		schema.addField(numberFieldSchema);
		schemaContainer("folder").setSchema(schema);
	}

	@Test
	@Override
	public void testUpdateNodeFieldWithField() {
		throw new NotImplementedException();
	}

	@Test
	@Override
	public void testUpdateNodeFieldWithNoField() {
		throw new NotImplementedException();
	}

	@Test
	@Override
	public void testCreateNodeWithField() {
		NodeResponse response = createNode("numberField", new NumberFieldImpl().setNumber("1.21"));
		NumberFieldImpl numberField = response.getField("numberField");
		assertEquals("1.21", numberField.getNumber());
	}

	@Test
	@Override
	public void testReadNodeWithExitingField() throws IOException {
		Node node = folder("2015");
		Schema schema = node.getSchema();
		NumberFieldSchema numberFieldSchema = new NumberFieldSchemaImpl();
		numberFieldSchema.setName("numberField");
		numberFieldSchema.setMin(10);
		numberFieldSchema.setMax(1000);
		numberFieldSchema.setRequired(true);
		schema.addField(numberFieldSchema);
		node.getSchemaContainer().setSchema(schema);

		NodeFieldContainer container = node.getFieldContainer(english());

		NumberField numberField = container.createNumber("numberField");
		numberField.setNumber("100.9");

		NodeRequestParameters parameters = new NodeRequestParameters();
		parameters.setLanguages("de");
		Future<NodeResponse> future = getClient().findNodeByUuid(DemoDataProvider.PROJECT_NAME, node.getUuid(), parameters);
		latchFor(future);
		assertSuccess(future);

		NumberFieldImpl deserializedNumberField = future.result().getField("numberField", NumberFieldImpl.class);
		assertNotNull(deserializedNumberField);
		assertEquals("100.9", deserializedNumberField.getNumber());
	}

}
