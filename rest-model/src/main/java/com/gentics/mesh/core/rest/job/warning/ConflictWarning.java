package com.gentics.mesh.core.rest.job.warning;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * POJO for a job conflict.
 */
public class ConflictWarning extends JobWarning {

	public ConflictWarning() {
	}

	@Override
	public String getType() {
		return "node-conflict-resolution";
	}

	@Override
	public String getMessage() {
		return "Encountered and resolved a conflict for node {" + getNodeUuid() + "}";
	}

	@JsonIgnore
	public String getNodeUuid() {
		return getProperties().get("nodeUuid");
	}

	@JsonIgnore
	public ConflictWarning setNodeUuid(String uuid) {
		getProperties().put("nodeUuid", uuid);
		return this;
	}

	@JsonIgnore
	public String getLanguageTag() {
		return getProperties().get("languageTag");
	}

	@JsonIgnore
	public ConflictWarning setLanguageTag(String languageTag) {
		getProperties().put("languageTag", languageTag);
		return this;
	}

	@JsonIgnore
	public String getBranchUuid() {
		return getProperties().get("branchUuid");
	}

	@JsonIgnore
	public ConflictWarning setBranchUuid(String branchUuid) {
		getProperties().put("branchUuid", branchUuid);
		return this;
	}

	@JsonIgnore
	public String getNodeType() {
		return getProperties().get("nodeType");
	}

	@JsonIgnore
	public ConflictWarning setNodeType(String type) {
		getProperties().put("nodeType", type);
		return this;
	}

	@JsonIgnore
	public String getFieldName() {
		return getProperties().get("fieldName");
	}

	@JsonIgnore
	public ConflictWarning setFieldName(String fieldName) {
		getProperties().put("fieldName", fieldName);
		return this;
	}

}
