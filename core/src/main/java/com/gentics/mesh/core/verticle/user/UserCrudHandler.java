package com.gentics.mesh.core.verticle.user;

import static com.gentics.mesh.core.data.relationship.GraphPermission.READ_PERM;
import static com.gentics.mesh.core.rest.error.HttpStatusCodeErrorException.failedFuture;
import static com.gentics.mesh.util.VerticleHelper.hasSucceeded;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.gentics.mesh.core.data.MeshVertex;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.data.root.MeshRoot;
import com.gentics.mesh.core.rest.user.UserPermissionResponse;
import com.gentics.mesh.core.verticle.handler.AbstractCrudHandler;
import com.gentics.mesh.handler.InternalActionContext;
import com.gentics.mesh.json.JsonUtil;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Component
public class UserCrudHandler extends AbstractCrudHandler {

	private static final Logger log = LoggerFactory.getLogger(UserCrudHandler.class);

	@Override
	public void handleDelete(InternalActionContext ac) {
		deleteElement(ac, () -> boot.userRoot(), "uuid", "user_deleted");
	}

	@Override
	public void handleCreate(InternalActionContext ac) {
		createElement(ac, () -> boot.userRoot());
	}

	@Override
	public void handleUpdate(InternalActionContext ac) {
		updateElement(ac, "uuid", () -> boot.userRoot());
	}

	@Override
	public void handleRead(InternalActionContext ac) {
		readElement(ac, "uuid", () -> boot.userRoot());
	}

	@Override
	public void handleReadList(InternalActionContext ac) {
		readElementList(ac, () -> boot.userRoot());
	}

	public void handlePermissionRead(InternalActionContext ac) {
		db.asyncNoTrx(tc -> {
			String userUuid = ac.getParameter("param0");
			String pathToElement = ac.getParameter("param1");
			if (StringUtils.isEmpty(userUuid)) {
				ac.fail(BAD_REQUEST, "error_uuid_must_be_specified");
			} else if (StringUtils.isEmpty(pathToElement)) {
				ac.fail(BAD_REQUEST, "user_permission_path_missing");
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Handling permission request for element on path {" + pathToElement + "}");
				}
				// 1. Load the role that should be used - read perm implies that the user is able to read the attached permissions
				boot.userRoot().loadObjectByUuid(ac, userUuid, READ_PERM, rh -> {
					if (hasSucceeded(ac, rh)) {

						db.noTrx(noTx -> {
							// 2. Resolve the path to element that is targeted
							MeshRoot.getInstance().resolvePathToElement(pathToElement, vertex -> {
								if (hasSucceeded(ac, vertex)) {
									if (vertex.result() == null) {
										ac.errorHandler().handle(failedFuture(NOT_FOUND, "error_element_for_path_not_found", pathToElement));
										return;
									}
									MeshVertex targetElement = vertex.result();
									User user = rh.result();
									UserPermissionResponse response = new UserPermissionResponse();
									for (GraphPermission perm : user.getPermissions(ac, targetElement)) {
										response.getPermissions().add(perm.getSimpleName());
									}
									ac.send(JsonUtil.toJson(response));
								}
							});

						});

					}
				});
			}
		} , ac.errorHandler());
	}

}
