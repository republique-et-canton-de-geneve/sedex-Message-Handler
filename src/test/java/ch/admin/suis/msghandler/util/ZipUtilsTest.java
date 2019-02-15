package ch.admin.suis.msghandler.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * TODO: javadoc
 *
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */

public class ZipUtilsTest {

    private static final String DESTINATION = "uncompressed";

    private static final String BASE_DIR = System.getProperty("java.io.tmpdir");

    @Test
    public void testDecompress() throws Exception {
        final File destDir = new File(BASE_DIR, DESTINATION + "-" + System.currentTimeMillis());
        destDir.mkdirs();
        destDir.deleteOnExit();

        final File compressed = new File(ZipUtilsTest.class.getResource("/files/upload.zip").toURI());
        List<File> createdFiles = ZipUtils.decompress(compressed, destDir);
        for (File file : createdFiles){
            file.delete();
        }
        final File stupidFilesFolder = new File(destDir + "/files");
        stupidFilesFolder.delete();
    }
}
