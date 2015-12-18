package com.gentics.mesh.core.verticle.tagfamily;

import static com.gentics.mesh.core.data.relationship.GraphPermission.READ_PERM;
import static com.gentics.mesh.util.VerticleHelper.hasSucceeded;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import org.springframework.stereotype.Component;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.MeshAuthUser;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Tag;
import com.gentics.mesh.core.data.TagFamily;
import com.gentics.mesh.core.verticle.handler.AbstractCrudHandler;
import com.gentics.mesh.handler.InternalActionContext;
import com.gentics.mesh.query.impl.PagingParameter;

@Component
public class TagFamilyCrudHandler extends AbstractCrudHandler {

	@Override
	public void handleCreate(InternalActionContext ac) {
		createElement(ac, () -> ac.getProject().getTagFamilyRoot());
	}

	@Override
	public void handleDelete(InternalActionContext ac) {
		deleteElement(ac, () -> ac.getProject().getTagFamilyRoot(), "uuid", "tagfamily_deleted");
	}

	@Override
	public void handleUpdate(InternalActionContext ac) {
		updateElement(ac, "uuid", () -> ac.getProject().getTagFamilyRoot());
	}

	@Override
	public void handleRead(InternalActionContext ac) {
		readElement(ac, "uuid", () -> ac.getProject().getTagFamilyRoot());
	}

	@Override
	public void handleReadList(InternalActionContext ac) {
		Project project = ac.getProject();
		readElementList(ac, () -> project.getTagFamilyRoot());
	}

	public void handleReadTagList(InternalActionContext ac) {
		db.asyncNoTrx(tx -> {
			Project project = ac.getProject();
			MeshAuthUser requestUser = ac.getUser();
			PagingParameter pagingInfo = ac.getPagingParameter();

			// TODO this is not checking for the project name and project relationship. We _need_ to fix this!
			project.getTagFamilyRoot().loadObject(ac, "tagFamilyUuid", READ_PERM, rh -> {
				if (hasSucceeded(ac, rh)) {
					TagFamily tagFamily = rh.result();
					try {
						Page<? extends Tag> tagPage = tagFamily.getTags(requestUser, pagingInfo);
						tagPage.transformAndRespond(ac, OK);
					} catch (Exception e) {
						ac.fail(e);
					}
				}
			});
		} , ac.errorHandler());
	}
}
