/*
 * Copyright (C) 2006-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * $Id: CompleteFullTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.common;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Ignore;
import org.junit.Test;

import ch.admin.suis.msghandler.sender.SenderSession;
import ch.admin.suis.msghandler.util.V2MessageXmlGenerator;
import ch.admin.suis.msghandler.util.ZipUtils;

/**
 * Unit test for the
 * <code>ClientConfigurationFactory</code> class.
 * <p>
 * This is like an integration test. Somewhere should be a document which describes which files have to be on the right
 * place. Document name: "completeTest.txt".
 * "src/test/resources/complete". This may help you to understand...
 * <p>
 * This unit test tests a lot of the MH use-cases. Multiple Outbox directories with multiple signing Outbox directories,
 * Filename duplications, and so on...
 * <p>
 * For more details: ./src/test/resources/complete/README_TestComplete.txt
 *
 * @author Kasimir Blaser
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
@Ignore // TODO : REMOVE THIS ONCE TICKET CONCERNING JAVA LIMITATIONS IS FIXED
public class CompleteFullTest extends CompleteBasicTest {

    private static final String BASE_PATH_MH = "./src/test/resources/complete/mh/base-path";

    private static final String INSTALL_DIR = "./src/test/resources/complete/mh/install-dir";

    private static final String BASE_PATH_SDX = "./src/test/resources/complete/sedex";

    private static final String BASE_PATH_SETUP = "./src/test/resources/complete/initData";

    private static final List<File> TEMP_DIRS = Arrays.asList(
	      new File(BASE_PATH_MH, "corrupted"),
	      new File(BASE_PATH_MH, "outbox1"),
	      new File(BASE_PATH_MH, "inbox1"),
	      new File(BASE_PATH_MH, "outbox2"),
	      new File(BASE_PATH_MH, "inbox3"),
	      new File(BASE_PATH_MH, "outbox3"),
	      new File(BASE_PATH_MH, "inbox4"),
	      new File(BASE_PATH_MH, "inbox5"),
	      new File(BASE_PATH_MH, "outboxTransparent"),
	      new File(BASE_PATH_MH, "outboxTransparent2"),
	      new File(BASE_PATH_MH, "receipts"),
	      new File(BASE_PATH_MH, "sent"),
	      new File(BASE_PATH_MH, "signingOutbox1_1"),
	      new File(BASE_PATH_MH, "signingOutbox1_2"),
	      new File(BASE_PATH_MH, "signingOutbox2"),
	      new File(BASE_PATH_MH, "signingOutbox2Processed"),
	      new File(BASE_PATH_MH, "tmp/preparing"),
	      new File(BASE_PATH_MH, "tmp/receiving"),
	      new File(BASE_PATH_MH, "inbox2"),
	      new File(BASE_PATH_MH, "inbox2a"),
	      new File(BASE_PATH_MH, "receipts"),
	      new File(BASE_PATH_MH, "unknown"),
	      new File(BASE_PATH_SDX, "inbox"),
	      new File(BASE_PATH_SDX, "outbox"),
	      new File(BASE_PATH_SDX, "receipts"),
	      new File(BASE_PATH_SDX, "sent"),
	      new File(BASE_PATH_MH, "../../DB"),
	      new File(BASE_PATH_SETUP, "filesForOutbox3"));

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Security.addProvider(new BouncyCastleProvider());
        SenderSession.msgGen = new V2MessageXmlGenerator();

        TEMP_DIRS.forEach((dir) ->
        {
          try
          {
            FileUtils.forceMkdir(dir);
          } catch (IOException ex)
          {
            // ignore
          }
        });

        addToClassPath(INSTALL_DIR + "/conf");
    }

    @Override
    protected void tearDown() throws Exception {
	    super.tearDown();
	        // Lösche die für die Tests erforderlichen Verzeichnisse rekursiv
	    TEMP_DIRS.forEach((dir) ->
	        {
	          try
	          {
	            FileUtils.deleteDirectory(dir);
	          } catch (IOException ex)
	          {
	            // ignore
	          }
	    });
   }

    @Test
    public void testComplete() throws IOException, ConfigurationException, InterruptedException {
        PropertyConfigurator.configureAndWatch(INSTALL_DIR + "/conf/log4j.properties");
        initialize();
        validateBeforeRun();

        MessageHandlerService mhs = new MessageHandlerService();
        Integer result = mhs.start(new String[]{INSTALL_DIR + "/conf/config.xml"});
        assertNull(result);
        
        Thread.sleep(20 * 1000);  //30 seconds
        assertEquals(0, mhs.stop(0));
        validateAfterRun();
    }

    private void validateBeforeRun() throws FileNotFoundException {
        assertEquals(1, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox1")).size());
        assertEquals(1, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox2")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox3")).size());
        assertEquals(4, getAllFilesFromDir(new File(BASE_PATH_MH, "outboxTransparent")).size());

        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox1_1")).size());
        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox1_2")).size());
        assertEquals(3, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox2")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox2Processed")).size());

        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "receipts")).size());
        assertEquals(1, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox2a")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_SDX, "receipts")).size());
    }

    private void validateAfterRun() throws FileNotFoundException, IOException {
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox1")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox2")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox3")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outboxTransparent")).size());

        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox1_1")).size());
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox1_2")).size());
        assertEquals(1, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox2")).size());
        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_MH, "signingOutbox2Processed")).size());

        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox2a")).size());
        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_MH, "inbox2")).size());
        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_SDX, "receipts")).size());
        assertEquals(2, getAllFilesFromDir(new File(BASE_PATH_MH, "receipts")).size());
        /**
         * Check for .prot file. Config.xml: produceProtocol="true"
         */
        int cntOutFiles = 0;
        int cntProdFiles = 0;
        for (File sentFile : getAllFilesFromDir(new File(BASE_PATH_MH, "sent"))) {
            if (sentFile.getName().endsWith(".prot")) {
                cntProdFiles++;
            } else {
                cntOutFiles++;
            }
        }
        assertTrue(cntOutFiles == cntProdFiles);

        List<File> sedexOutFiles = getAllFilesFromDir(new File(BASE_PATH_SDX, "outbox"));
        assertEquals(2, sedexOutFiles.size()); //2 Zip and 2 Envelope files

//    boolean outbox1FilesComplete = false;
        boolean outbox2FilesComplete = false;
//    boolean outboxTransparentComplete = false;
        int dataFilesCounter = 0;
        int envFilesCounter = 0;

        for (File f : sedexOutFiles) {
            String fileName = f.getName();
            if (fileName.startsWith("data_") && fileName.endsWith(".zip")) {
                dataFilesCounter++;

                List<File> unzippedFiles = ZipUtils.decompress(f, new File(BASE_PATH_SDX, "outbox"));
//        if(unzippedFiles.size() == 1) {
//          outboxTransparentComplete = true;
//        }
                if (unzippedFiles.size() == 3) {
                    outbox2FilesComplete = true;
                }
//        else if(unzippedFiles.size() == 5) {
//          outbox1FilesComplete = true;
//        }
                else {
                    fail("There should be one zip files. With 3 files.");
                }
            } else if (fileName.startsWith("envl_") && fileName.endsWith(".xml")) {
                envFilesCounter++;
            }
        }

        assertEquals("There should be 1 envelope file", 1, envFilesCounter);
        assertEquals("There should be 1 data file", 1, dataFilesCounter);

//    assertTrue("There should be 5 files in the ZIP from outbox1", outbox1FilesComplete);
        assertTrue("There should be 3 files in the ZIP from outbox2", outbox2FilesComplete);
//    assertTrue("There should be 1 file in the ZIP from outboxTransparent", outboxTransparentComplete);
    }

    /**
     * Make a fresh directory/file structure.
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        List<File> files = getAllFilesFromDir(new File(BASE_PATH_SETUP + "/filesForOutbox1"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outbox1", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForOutbox2"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outbox2", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForOutbox3"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outbox3", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForSigningOutbox1_1"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/signingOutbox1_1", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForSigningOutbox1_2"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/signingOutbox1_2", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForSigningOutbox2"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/signingOutbox2", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForOutboxTransparent"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outboxTransparent", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForOutbox2a"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outbox2a", f.getName()));
        }
    }
}
