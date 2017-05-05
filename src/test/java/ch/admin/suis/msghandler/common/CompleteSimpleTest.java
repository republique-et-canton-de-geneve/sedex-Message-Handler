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
 * $Id: CompleteSimpleTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.util.ZipUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;

/**
 * Unit test for the
 * <code>ClientConfigurationFactory</code> class.
 * <p>
 * This is like an integration test. Somewhere should be a document which describes which files have to be on the right
 * place. Document name: "completeTest.txt". Use the linux "tree" command on the directory
 * "src/test/resources/complete". This may help you to understand...
 * <p>
 * This is a basic test of the MH. Just a test with one Outbox directory with one file.
 * <p>
 * For more details: ./src/test/resources/complete/README_TestComplete.txt
 *
 * @author Kasimir Blaser
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class CompleteSimpleTest extends CompleteBasicTest {

    private static final String BASE_PATH_MH = "./src/test/resources/complete/mh-simple/base-path";

    private static final String INSTALL_DIR = "./src/test/resources/complete/mh-simple/install-dir";

    private static final String BASE_PATH_SDX = "./src/test/resources/complete/sedex";

    private static final String BASE_PATH_SETUP = "./src/test/resources/complete/initData";

    private List<File> dirsToClean = new ArrayList<File>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dirsToClean.add(new File(BASE_PATH_MH, "corrupted"));
        dirsToClean.add(new File(BASE_PATH_MH, "outbox1"));
        dirsToClean.add(new File(BASE_PATH_MH, "receipts"));
        dirsToClean.add(new File(BASE_PATH_MH, "sent"));
        dirsToClean.add(new File(BASE_PATH_MH, "tmp/preparing"));
        dirsToClean.add(new File(BASE_PATH_MH, "tmp/receiving"));

        dirsToClean.add(new File(BASE_PATH_SDX, "inbox"));
        dirsToClean.add(new File(BASE_PATH_SDX, "outbox"));
        dirsToClean.add(new File(BASE_PATH_SDX, "receipts"));
        dirsToClean.add(new File(BASE_PATH_SDX, "sent"));

        File dbFile = new File(BASE_PATH_MH, "../../DB");
        if (!dbFile.exists()) {
            dbFile.mkdir();
        }
        dirsToClean.add(dbFile);

        addToClassPath(INSTALL_DIR + "/conf");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanDirectories(dirsToClean);
    }

    public void testComplete() throws IOException, ConfigurationException, InterruptedException {
        PropertyConfigurator.configureAndWatch(INSTALL_DIR + "/conf/log4j.properties");

        cleanDirectories(dirsToClean);
        initialize();
        validateBeforeRun();

        MessageHandlerService mhs = new MessageHandlerService();
        Integer result = mhs.start(new String[]{INSTALL_DIR + "/conf/config.xml"});
        Thread.sleep(10 * 1000);  //10 seconds
        result = mhs.stop(0);
        assertTrue(0 == result);
        validateAfterRun();
    }

    private void validateBeforeRun() throws FileNotFoundException {
        assertEquals(1, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox1")).size());
    }

    private void validateAfterRun() throws FileNotFoundException, IOException {
        assertEquals(0, getAllFilesFromDir(new File(BASE_PATH_MH, "outbox1")).size());

        List<File> sedexOutFiles = getAllFilesFromDir(new File(BASE_PATH_SDX, "outbox"));
        assertEquals(2, sedexOutFiles.size());

        boolean dataAvailable = false;
        boolean envAvailable = false;
        for (File f : sedexOutFiles) {
            String fileName = f.getName();
            if (fileName.startsWith("data_") && fileName.endsWith(".zip")) {
                dataAvailable = true;

                List<File> unzippedFiles = ZipUtils.decompress(f, new File(BASE_PATH_SDX, "outbox"));
                assertEquals("There should be one file in the data ZIP", 1, unzippedFiles.size());
            } else if (fileName.startsWith("envl_") && fileName.endsWith(".xml")) {
                envAvailable = true;
            }
        }

        assertTrue("No data file available", dataAvailable);
        assertTrue("No envelope file available", envAvailable);

        String name1 = sedexOutFiles.get(0).getName();
        String name2 = sedexOutFiles.get(1).getName();

        String uuid1 = name1.substring(name1.indexOf("_") + 1, name1.lastIndexOf("."));
        String uuid2 = name2.substring(name2.indexOf("_") + 1, name2.lastIndexOf("."));

        assertEquals("Both UUIDs have to be equal", uuid1, uuid2);

        /**
         * Check for .prot file. They shouldn't be generated. Config.xml:  produceProtocol="false"
         */
        int cntProdFiles = 0;
        for (File sentFile : getAllFilesFromDir(new File(BASE_PATH_MH, "sent"))) {
            if (sentFile.getName().endsWith(".prot")) {
                cntProdFiles++;
            }
        }
        assertEquals(0, cntProdFiles);
    }

    /**
     * Make a fresh directory/file structure.
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        List<File> files = getAllFilesFromDir(new File(BASE_PATH_SETUP, "filesForOutbox1"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH_MH + "/outbox1", f.getName()));
            break; //just one
        }
    }
}
