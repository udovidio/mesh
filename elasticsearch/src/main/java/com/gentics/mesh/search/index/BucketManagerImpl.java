package com.gentics.mesh.search.index;

import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.etc.config.MeshOptions;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.search.BucketableElement;

import io.reactivex.Flowable;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class BucketManagerImpl implements BucketManager {

	private static final Logger log = LoggerFactory.getLogger(BucketManagerImpl.class);

	private final MeshOptions options;

	private final Database database;

	@Inject
	public BucketManagerImpl(MeshOptions options, Database database) {
		this.options = options;
		this.database = database;
	}

	private int getBucketCount(long elementCount) {
		long batchSize = batchSize();
		if (batchSize <= 0) {
			return 1;
		}
		double sizeP = (double) elementCount / (double) batchSize;
		sizeP = Math.ceil(sizeP);
		int size = (int) sizeP;
		// Cap to 1 partition
		if (size == 0) {
			size = 1;
		}
		return size;
	}

	private int batchSize() {
		return options.getSearchOptions().getSyncBatchSize();
	}

	@Override
	public Flowable<Bucket> getBuckets(Class<? extends BucketableElement> clazz) {
		long count = database.tx(() -> database.count(clazz));
		int bucketCount = getBucketCount(count);
		int bucketSize = Integer.MAX_VALUE / bucketCount;
		log.debug("Calculated {" + bucketCount + "} buckets are needed for {" + count + "} elements and batch size of {" + batchSize() + "}");
		return Flowable.range(0, bucketCount).map(bucket -> {
			long start = bucketSize * bucket.intValue();
			long end = start - 1 + bucketSize;
			// Cap to end to prevent issues with loss of precision during division
			if (bucketCount - 1 == bucket) {
				end = Integer.MAX_VALUE;
			}
			return new Bucket(start, end);
		});
	}

}
