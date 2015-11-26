package com.gentics.mesh.core.node;

import static com.gentics.mesh.demo.TestDataProvider.PROJECT_NAME;
import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.failingLatch;
import static com.gentics.mesh.util.MeshAssert.latchFor;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.gentics.mesh.Mesh;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.node.NodeDownloadResponse;
import com.gentics.mesh.etc.config.ImageManipulatorOptions;
import com.gentics.mesh.query.impl.ImageRequestParameter;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class NodeImageResizeVerticleTest extends AbstractBinaryVerticleTest {

	private static final Logger log = LoggerFactory.getLogger(NodeVerticleTest.class);

	@Test
	public void testImageResize() throws Exception {
		Node node = folder("news");

		// 1. Upload image
		uploadImage(node);

		// 2. Resize image
		ImageRequestParameter params = new ImageRequestParameter().setWidth(100).setHeight(102);
		Future<NodeDownloadResponse> download = resizeImage(node, params);

		// 3. Validate resize
		validateResizeImage(download.result(), node, params, 100, 102);
	}

	@Test
	public void testImageResizeOverLimit() throws Exception {
		Node node = folder("news");
		ImageManipulatorOptions options = Mesh.mesh().getOptions().getImageOptions();
		// 1. Upload image
		uploadImage(node);

		// 2. Resize image
		ImageRequestParameter params = new ImageRequestParameter().setWidth(options.getMaxWidth() + 1).setHeight(102);
		Future<NodeDownloadResponse> download = resizeImage(node, params);
		expectException(download, BAD_REQUEST, "image_error_width_limit_exceeded", String.valueOf(options.getMaxWidth()),
				String.valueOf(options.getMaxWidth() + 1));

	}

	@Test
	public void testImageExactLimit() throws Exception {
		Node node = folder("news");
		ImageManipulatorOptions options = Mesh.mesh().getOptions().getImageOptions();
		// 1. Upload image
		uploadImage(node);

		// 2. Resize image
		ImageRequestParameter params = new ImageRequestParameter().setWidth(options.getMaxWidth()).setHeight(102);
		Future<NodeDownloadResponse> download = resizeImage(node, params);
		validateResizeImage(download.result(), node, params, 2048, 102);

	}

	private Future<NodeDownloadResponse> resizeImage(Node node, ImageRequestParameter params) {
		Future<NodeDownloadResponse> downloadFuture = getClient().downloadBinaryField(PROJECT_NAME, node.getUuid(), params);
		latchFor(downloadFuture);
		return downloadFuture;
	}

	private void validateResizeImage(NodeDownloadResponse download, Node node, ImageRequestParameter params, int expectedWidth, int expectedHeight)
			throws Exception {
		node.reload();
		File targetFile = new File("target", UUID.randomUUID() + "_resized.jpg");
		CountDownLatch latch = new CountDownLatch(1);
		Mesh.vertx().fileSystem().writeFile(targetFile.getAbsolutePath(), download.getBuffer(), rh -> {
			assertTrue(rh.succeeded());
			latch.countDown();
		});
		failingLatch(latch);
		assertThat(targetFile).exists();
		BufferedImage img = ImageIO.read(targetFile);
		assertEquals(expectedWidth, img.getWidth());
		assertEquals(expectedHeight, img.getHeight());

		File cacheFile = springConfig.imageProvider().getCacheFile(node.getBinarySHA512Sum(), params);
		assertTrue("The cache file could not be found in the cache directory. {" + cacheFile.getAbsolutePath() + "}", cacheFile.exists());
	}

	private void uploadImage(Node node) throws IOException {
		String contentType = "image/jpeg";
		String fileName = "blume.jpg";
		prepareSchema(node, true, "image/.*");
		resetClientSchemaStorage();

		System.out.println(node.getBinarySegmentedPath());

		InputStream ins = getClass().getResourceAsStream("/pictures/blume.jpg");
		byte[] bytes = IOUtils.toByteArray(ins);
		Buffer buffer = Buffer.buffer(bytes);

		Future<GenericMessageResponse> future = getClient().updateNodeBinaryField(PROJECT_NAME, node.getUuid(), buffer, fileName, contentType);
		latchFor(future);
		assertSuccess(future);
	}

}