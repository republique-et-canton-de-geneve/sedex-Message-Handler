package ch.admin.suis.msghandler.util;

import java.io.File;

import org.junit.Test;

public class ZipUtilsTest {

	private static final String DESTINATION = "uncompressed";

	private static final String BASE_DIR = System.getProperty("java.io.tmpdir");

	@Test
	public void testDecompress() throws Exception {
		final File destDir = new File(BASE_DIR,
				DESTINATION + "-" + System.currentTimeMillis());
		destDir.mkdirs();
		destDir.deleteOnExit();

		final File compressed = new File("target/test-classes/files/upload.zip");
		ZipUtils.decompress(compressed, destDir);
	}
}
