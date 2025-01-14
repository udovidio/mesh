package com.gentics.mesh.core.verticle.job;

import static com.gentics.mesh.core.rest.MeshEvent.JOB_WORKER_ADDRESS;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.core.db.Database;
import com.gentics.mesh.core.jobs.JobProcessor;
import com.gentics.mesh.verticle.AbstractJobVerticle;

import dagger.Lazy;
import io.reactivex.Completable;
import io.vertx.core.eventbus.Message;

/**
 * Dedicated verticle which will process jobs.
 */
@Singleton
public class JobWorkerVerticleImpl extends AbstractJobVerticle implements JobWorkerVerticle {

	private static final String GLOBAL_JOB_LOCK_NAME = "mesh.internal.joblock";

	public final static String PROJECT_UUID_HEADER = "projectUuid";

	public final static String BRANCH_UUID_HEADER = "branchUuid";

	public final static String UUID_HEADER = "uuid";

	public static final String FROM_VERSION_UUID_HEADER = "fromVersion";

	public static final String TO_VERSION_UUID_HEADER = "toVersion";

	private Lazy<BootstrapInitializer> boot;
	private JobProcessor jobProcessor;
	private Database db;

	@Inject
	public JobWorkerVerticleImpl(Database db, Lazy<BootstrapInitializer> boot, JobProcessor jobProcessor) {
		this.db = db;
		this.boot = boot;
		this.jobProcessor = jobProcessor;
	}

	@Override
	public String getJobAdress() {
		return JOB_WORKER_ADDRESS + boot.get().mesh().getOptions().getNodeName();
	}

	@Override
	public String getLockName() {
		return GLOBAL_JOB_LOCK_NAME;
	}

	@Override
	public Completable executeJob(Message<Object> message) {
		return Completable.defer(() -> jobProcessor.process());
	}

}
