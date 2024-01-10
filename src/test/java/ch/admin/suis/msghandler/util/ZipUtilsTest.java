package ch.admin.suis.msghandler.util;

import java.io.File;
import java.net.URL;

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

		final URL url = ZipUtilsTest.class.getResource("/files/upload.zip");
		final File compressed = new File(url.getFile());
		ZipUtils.decompress(compressed, destDir);
	}
}
