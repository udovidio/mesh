package com.gentics.mesh.core.field.bool;

import static com.gentics.mesh.core.field.bool.BooleanListFieldHelper.CREATE_EMPTY;
import static com.gentics.mesh.core.field.bool.BooleanListFieldHelper.FETCH;
import static com.gentics.mesh.core.field.bool.BooleanListFieldHelper.FILL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.HibNodeFieldContainer;
import com.gentics.mesh.core.data.dao.ContentDao;
import com.gentics.mesh.core.data.node.HibNode;
import com.gentics.mesh.core.data.node.field.list.HibBooleanFieldList;
import com.gentics.mesh.core.db.Tx;
import com.gentics.mesh.core.field.AbstractFieldTest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.field.Field;
import com.gentics.mesh.core.rest.node.field.list.impl.BooleanFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.StringFieldListImpl;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.test.MeshTestSetting;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.NoConsistencyCheck;
import com.gentics.mesh.util.CoreTestUtils;

@MeshTestSetting(testSize = TestSize.PROJECT_AND_NODE, startServer = false)
public class BooleanListFieldTest extends AbstractFieldTest<ListFieldSchema> {

	private static final String BOOLEAN_LIST = "booleanList";

	@Override
	protected ListFieldSchema createFieldSchema(boolean isRequired) {
		ListFieldSchema schema = new ListFieldSchemaImpl();
		schema.setListType("boolean");
		schema.setName(BOOLEAN_LIST);
		schema.setRequired(isRequired);
		return schema;
	}

	protected ListFieldSchema createFieldSchema(String fieldKey, boolean isRequired) {
		ListFieldSchema schema = new ListFieldSchemaImpl();
		schema.setListType("boolean");
		schema.setName(fieldKey);
		schema.setRequired(isRequired);
		return schema;
	}

	@Test
	@Override
	public void testFieldTransformation() throws Exception {
		try (Tx tx = tx()) {
			HibNode node = folder("2015");
			ContentDao contentDao = tx.contentDao();
			prepareNode(node, BOOLEAN_LIST, "boolean");
			HibNodeFieldContainer container = contentDao.createFieldContainer(node, english(),
					node.getProject().getLatestBranch(), user(),
					contentDao.getLatestDraftFieldContainer(node, english()), true);

			HibBooleanFieldList booleanList = container.createBooleanList(BOOLEAN_LIST);
			booleanList.createBoolean(true);
			booleanList.createBoolean(null);
			booleanList.createBoolean(false);
			tx.success();
		}

		try (Tx tx = tx()) {
			HibNode node = folder("2015");
			NodeResponse response = transform(node);
			assertList(2, BOOLEAN_LIST, "boolean", response);
		}
	}

	@Test
	@Override
	public void testFieldUpdate() throws Exception {
		try (Tx tx = tx()) {
			HibNodeFieldContainer container = CoreTestUtils.createContainer(createFieldSchema(true));
			HibBooleanFieldList list = container.createBooleanList(BOOLEAN_LIST);
			list.createBoolean(true);
			list.createBoolean(false);
			list.createBoolean(null);
			assertEquals("Only non-null values are persisted.", 2, list.getList().size());
			assertEquals(2, list.getSize());
			assertNotNull(list.getBoolean(1));
			assertTrue(list.getBoolean(1).getBoolean());
			list.removeAll();
			assertEquals(0, list.getSize());
			assertEquals(0, list.getList().size());
		}
	}

	@Test
	@NoConsistencyCheck
	@Override
	public void testClone() {
		try (Tx tx = tx()) {
			HibNodeFieldContainer container = CoreTestUtils.createContainer(createFieldSchema("testField", true));
			HibBooleanFieldList testField = container.createBooleanList("testField");
			testField.createBoolean(true);
			testField.createBoolean(false);

			HibNodeFieldContainer otherContainer = CoreTestUtils.createContainer(createFieldSchema("testField", true));
			testField.cloneTo(otherContainer);

			assertTrue(otherContainer.getBooleanList("testField").equals(testField));
		}
	}

	@Test
	@Override
	public void testEquals() {
		try (Tx tx = tx()) {
			HibNodeFieldContainer container = CoreTestUtils.createContainer(createFieldSchema("fieldA", true), createFieldSchema("fieldB", true));
			HibBooleanFieldList fieldA = container.createBooleanList("fieldA");
			HibBooleanFieldList fieldB = container.createBooleanList("fieldB");
			assertTrue("The field should  be equal to itself", fieldA.equals(fieldA));
			fieldA.addItem(fieldA.createBoolean(true));
			assertTrue("The field should  still be equal to itself", fieldA.equals(fieldA));

			assertFalse("The field should not be equal to a non-string field", fieldA.equals("bogus"));
			assertFalse("The field should not be equal since fieldB has no value", fieldA.equals(fieldB));
			fieldB.addItem(fieldB.createBoolean(true));
			assertTrue("Both fields have the same value and should be equal", fieldA.equals(fieldB));
		}
	}

	@Test
	@Override
	public void testEqualsNull() {
		try (Tx tx = tx()) {
			HibNodeFieldContainer container = CoreTestUtils.createContainer(createFieldSchema(true));
			HibBooleanFieldList fieldA = container.createBooleanList(BOOLEAN_LIST);
			assertFalse(fieldA.equals((Field) null));
			assertFalse(fieldA.equals((HibBooleanFieldList) null));
		}
	}

	@Test
	@Override
	public void testEqualsRestField() {
		try (Tx tx = tx()) {
			HibNodeFieldContainer container = CoreTestUtils.createContainer(createFieldSchema(true));
			Boolean dummyValue = true;

			// rest null - graph null
			HibBooleanFieldList fieldA = container.createBooleanList(BOOLEAN_LIST);

			BooleanFieldListImpl restField = new BooleanFieldListImpl();
			assertTrue("Both fields should be equal to eachother since both values are null", fieldA.equals(restField));

			// rest set - graph set - different values
			fieldA.addItem(fieldA.createBoolean(dummyValue));
			restField.add(false);
			assertFalse("Both fields should be different since both values are not equal", fieldA.equals(restField));

			// rest set - graph set - same value
			restField.getItems().clear();
			restField.add(dummyValue);
			assertTrue("Both fields should be equal since values are equal", fieldA.equals(restField));

			StringFieldListImpl otherTypeRestField = new StringFieldListImpl();
			otherTypeRestField.add("true");
			// rest set - graph set - same value different type
			assertFalse("Fields should not be equal since the type does not match.", fieldA.equals(otherTypeRestField));
		}
	}

	@Test
	@Override
	public void testUpdateFromRestNullOnCreate() {
		try (Tx tx = tx()) {
			invokeUpdateFromRestTestcase(BOOLEAN_LIST, FETCH, CREATE_EMPTY);
		}
	}

	@Test
	@Override
	public void testUpdateFromRestNullOnCreateRequired() {
		try (Tx tx = tx()) {
			invokeUpdateFromRestNullOnCreateRequiredTestcase(BOOLEAN_LIST, FETCH);
		}
	}

	@Test
	@Override
	public void testRemoveFieldViaNull() {
		try (Tx tx = tx()) {
			InternalActionContext ac = mockActionContext();
			invokeRemoveFieldViaNullTestcase(BOOLEAN_LIST, FETCH, FILL, (node) -> {
				updateContainer(ac, node, BOOLEAN_LIST, null);
			});
		}
	}

	@Test
	@Override
	public void testRemoveRequiredFieldViaNull() {
		try (Tx tx = tx()) {
			InternalActionContext ac = mockActionContext();
			invokeRemoveRequiredFieldViaNullTestcase(BOOLEAN_LIST, FETCH, FILL, (container) -> {
				updateContainer(ac, container, BOOLEAN_LIST, null);
			});
		}
	}

	@Test
	@Override
	public void testUpdateFromRestValidSimpleValue() {
		try (Tx tx = tx()) {
			InternalActionContext ac = mockActionContext();
			invokeUpdateFromRestValidSimpleValueTestcase(BOOLEAN_LIST, FILL, (container) -> {
				BooleanFieldListImpl field = new BooleanFieldListImpl();
				field.getItems().add(true);
				field.getItems().add(false);
				updateContainer(ac, container, BOOLEAN_LIST, field);
			}, (container) -> {
				HibBooleanFieldList field = container.getBooleanList(BOOLEAN_LIST);
				assertNotNull("The graph field {" + BOOLEAN_LIST + "} could not be found.", field);
				assertEquals("The list of the field was not updated.", 2, field.getList().size());
				assertEquals("The list item of the field was not updated.", true, field.getList().get(0).getBoolean());
				assertEquals("The list item of the field was not updated.", false, field.getList().get(1).getBoolean());
			});
		}
	}

}
