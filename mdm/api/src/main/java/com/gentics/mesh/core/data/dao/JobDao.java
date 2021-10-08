package com.gentics.mesh.core.data.dao;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Predicate;

import com.gentics.mesh.context.BulkActionContext;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.branch.HibBranch;
import com.gentics.mesh.core.data.job.HibJob;
import com.gentics.mesh.core.data.page.Page;
import com.gentics.mesh.core.data.perm.InternalPermission;
import com.gentics.mesh.core.data.project.HibProject;
import com.gentics.mesh.core.data.schema.HibMicroschema;
import com.gentics.mesh.core.data.schema.HibMicroschemaVersion;
import com.gentics.mesh.core.data.schema.HibSchema;
import com.gentics.mesh.core.data.schema.HibSchemaVersion;
import com.gentics.mesh.core.data.user.HibUser;
import com.gentics.mesh.core.rest.job.JobResponse;
import com.gentics.mesh.core.rest.job.JobWarningList;
import com.gentics.mesh.event.EventQueueBatch;
import com.gentics.mesh.parameter.PagingParameters;

import io.reactivex.Completable;

/**
 * DAO for {@link HibJob}.
 */
public interface JobDao extends DaoGlobal<HibJob>, DaoTransformable<HibJob, JobResponse> {

	/**
	 * Load a page of jobs.
	 * 
	 * @param ac
	 * @param pagingInfo
	 * @return
	 */
	Page<? extends HibJob> findAll(InternalActionContext ac, PagingParameters pagingInfo);

	/**
	 * Load a page of jobs.
	 * 
	 * @param ac
	 * @param pagingInfo
	 * @param extraFilter
	 * @return
	 */
	Page<? extends HibJob> findAll(InternalActionContext ac, PagingParameters pagingInfo, Predicate<HibJob> extraFilter);
	
	/**
	 * Find all elements and return a paged result. No permission check will be performed.
	 * 
	 * @param ac
	 *            action context
	 * @param pagingInfo
	 *            Paging information object that contains page options
	 * @return
	 */
	Page<? extends HibJob> findAllNoPerm(InternalActionContext ac, PagingParameters pagingInfo);

	/**
	 * Find the job by name.
	 * 
	 * @param name
	 * @return
	 */
	HibJob findByName(String name);

	/**
	 * Load the job by uuid.
	 * 
	 * @param ac
	 * @param uuid
	 * @param perm
	 * @param errorIfNotFound
	 * @return
	 */
	HibJob loadObjectByUuid(InternalActionContext ac, String uuid, InternalPermission perm, boolean errorIfNotFound);

	/**
	 * Load the job by uuid.
	 * 
	 * @param uuid
	 * @param errorIfNotFound
	 * @return
	 */
	HibJob loadObjectByUuidNoPerm(String uuid, boolean errorIfNotFound);

	/**
	 * Enqueue the microschema/micronode migration.
	 * 
	 * @param creator
	 * @param branch
	 * @param fromVersion
	 * @param toVersion
	 * @return
	 */
	HibJob enqueueMicroschemaMigration(HibUser creator, HibBranch branch, HibMicroschemaVersion fromVersion, HibMicroschemaVersion toVersion);

	/**
	 * Enqueue the branch migration.
	 * 
	 * @param creator
	 * @param branch
	 * @param fromVersion
	 * @param toVersion
	 * @return
	 */
	HibJob enqueueBranchMigration(HibUser creator, HibBranch branch, HibSchemaVersion fromVersion, HibSchemaVersion toVersion);

	/**
	 * Enqueue the schema/node migration.
	 * 
	 * @param creator
	 * @param branch
	 * @param fromVersion
	 * @param toVersion
	 * @return
	 */
	HibJob enqueueSchemaMigration(HibUser creator, HibBranch branch, HibSchemaVersion fromVersion, HibSchemaVersion toVersion);

	/**
	 * Enqueue the branch migration.
	 * 
	 * @param creator
	 * @param branch
	 * @return
	 */
	HibJob enqueueBranchMigration(HibUser creator, HibBranch branch);

	/**
	 * Update the job.
	 * 
	 * @param job
	 * @param ac
	 * @param batch
	 * @return
	 */
	boolean update(HibJob job, InternalActionContext ac, EventQueueBatch batch);

	/**
	 * Delete the job.
	 * 
	 * @param job
	 * @param bac
	 */
	void delete(HibJob job, BulkActionContext bac);

	/**
	 * Create a new job.
	 * 
	 * @param ac
	 * @param batch
	 * @param uuid
	 * @return
	 */
	HibJob create(InternalActionContext ac, EventQueueBatch batch, String uuid);

	/**
	 * Enqueue a project version purge job that is limited to the given date.
	 * 
	 * @param user
	 * @param project
	 * @param before
	 * @return
	 */
	HibJob enqueueVersionPurge(HibUser user, HibProject project, ZonedDateTime before);

	/**
	 * Enqueue a project version purge job.
	 * @param user
	 * @param project
	 * @return
	 */
	HibJob enqueueVersionPurge(HibUser user, HibProject project);

	/**
	 * Process all remaining jobs.
	 */
	Completable process();

	/**
	 * Purge all failed jobs from the job root.
	 */
	void purgeFailed();

	/**
	 * Delete all jobs.
	 */
	void clear();

	@Override
	default JobResponse transformToRestSync(HibJob job, InternalActionContext ac, int level, String... languageTags) {
		JobResponse response = new JobResponse();
		response.setUuid(job.getUuid());

		HibUser creator = job.getCreator();
		if (creator != null) {
			response.setCreator(creator.transformToReference());
		} else {
			//log.error("The object {" + getClass().getSimpleName() + "} with uuid {" + getUuid() + "} has no creator. Omitting creator field");
		}

		String date = job.getCreationDate();
		response.setCreated(date);
		response.setErrorMessage(job.getErrorMessage());
		response.setErrorDetail(job.getErrorDetail());
		response.setType(job.getType());
		response.setStatus(job.getStatus());
		response.setStopDate(job.getStopDate());
		response.setStartDate(job.getStartDate());
		response.setCompletionCount(job.getCompletionCount());
		response.setNodeName(job.getNodeName());

		JobWarningList warnings = job.getWarnings();
		if (warnings != null) {
			response.setWarnings(warnings.getData());
		}

		Map<String, String> props = response.getProperties();
		HibBranch branch = job.getBranch();
		if (branch != null) {
			props.put("branchName", branch.getName());
			props.put("branchUuid", branch.getUuid());
		} else {
			log.debug("No referenced branch found.");
		}

		HibSchemaVersion toSchema = job.getToSchemaVersion();
		if (toSchema != null) {
			HibSchema container = toSchema.getSchemaContainer();
			props.put("schemaName", container.getName());
			props.put("schemaUuid", container.getUuid());
			props.put("fromVersion", job.getFromSchemaVersion().getVersion());
			props.put("toVersion", toSchema.getVersion());
		}

		HibMicroschemaVersion toMicroschema = job.getToMicroschemaVersion();
		if (toMicroschema != null) {
			HibMicroschema container = toMicroschema.getSchemaContainer();
			props.put("microschemaName", container.getName());
			props.put("microschemaUuid", container.getUuid());
			props.put("fromVersion", job.getFromMicroschemaVersion().getVersion());
			props.put("toVersion", toMicroschema.getVersion());
		}
		return response;
	}
}
