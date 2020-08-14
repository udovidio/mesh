package com.gentics.mesh.core.actions.impl;

import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.context.BulkActionContext;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.action.DAOActionContext;
import com.gentics.mesh.core.action.SchemaDAOActions;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.dao.SchemaDaoWrapper;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.data.page.TransformablePage;
import com.gentics.mesh.core.data.perm.InternalPermission;
import com.gentics.mesh.core.data.schema.Schema;
import com.gentics.mesh.core.db.Tx;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.event.EventQueueBatch;
import com.gentics.mesh.parameter.PagingParameters;

@Singleton
public class SchemaDAOActionsImpl implements SchemaDAOActions {

	@Inject
	public SchemaDAOActionsImpl() {
	}

	@Override
	public Schema loadByUuid(DAOActionContext ctx, String uuid, InternalPermission perm, boolean errorIfNotFound) {
		SchemaDaoWrapper schemaDao = ctx.tx().data().schemaDao();
		if (perm == null) {
			return schemaDao.findByUuid(uuid);
		} else {
			return schemaDao.loadObjectByUuid(ctx.ac(), uuid, perm, errorIfNotFound);
		}
	}

	@Override
	public Schema loadByName(DAOActionContext ctx, String name, InternalPermission perm, boolean errorIfNotFound) {
		SchemaDaoWrapper schemaDao = ctx.tx().data().schemaDao();
		if (perm == null) {
			return schemaDao.findByName(name);
		} else {
			throw new RuntimeException("Not supported");
		}
	}

	public TransformablePage<? extends Schema> loadAll(Tx tx, Project project, InternalActionContext ac, PagingParameters pagingInfo) {
		SchemaDaoWrapper schemaDao = tx.data().schemaDao();
		return schemaDao.findAll(ac, project, pagingInfo);
	}

	@Override
	public TransformablePage<? extends Schema> loadAll(DAOActionContext ctx, PagingParameters pagingInfo) {
		SchemaDaoWrapper schemaDao = ctx.tx().data().schemaDao();
		return ctx.tx().data().schemaDao().findAll(ctx.ac(), pagingInfo);
	}

	@Override
	public Page<? extends Schema> loadAll(DAOActionContext ctx, PagingParameters pagingInfo, Predicate<Schema> extraFilter) {
		return ctx.project().getSchemaContainerRoot().findAll(ctx.ac(), pagingInfo, extraFilter);
		// TODO scope to project
		// return tx.data().schemaDao().findAll(ac, pagingInfo, extraFilter);
	}

	@Override
	public boolean update(Tx tx, Schema element, InternalActionContext ac, EventQueueBatch batch) {
		// Updates are handled by dedicated migration code
		return false;
	}

	@Override
	public Schema create(Tx tx, InternalActionContext ac, EventQueueBatch batch, String uuid) {
		return tx.data().schemaDao().create(ac, batch, uuid);
	}

	@Override
	public void delete(Tx tx, Schema schema, BulkActionContext bac) {
		tx.data().schemaDao().delete(schema, bac);
	}

	@Override
	public SchemaResponse transformToRestSync(Tx tx, Schema schema, InternalActionContext ac, int level, String... languageTags) {
		// return tx.data().schemaDao().
		return schema.transformToRestSync(ac, level, languageTags);
	}

	@Override
	public String getAPIPath(Tx tx, InternalActionContext ac, Schema element) {
		return element.getAPIPath(ac);
	}

	@Override
	public String getETag(Tx tx, InternalActionContext ac, Schema element) {
		return element.getETag(ac);
	}

}
