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
 * $Id: CompleteMultiReceiverTest.java 327 2014-01-27 13:07:13Z blaser $
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
public abstract class CompleteMultiReceiverTest extends CompleteBasicTest {

    static final String BASE = "./src/test/resources/complete/mh-multi/";

    static final String BASE_PATH_MH = BASE + "/base-path";

    static final String INSTALL_DIR = BASE + "/install-dir";

    static final String BASE_PATH_SDX = "./src/test/resources/complete/sedex";

    static final String BASE_PATH_SETUP = BASE + "/initData";

    private List<File> dirsToClean = new ArrayList<File>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dirsToClean.add(new File(BASE_PATH_MH, "nativeApp1/inbox"));
        dirsToClean.add(new File(BASE_PATH_MH, "nativeApp1/outbox"));

        dirsToClean.add(new File(BASE_PATH_MH, "transApp1/inbox"));
        dirsToClean.add(new File(BASE_PATH_MH, "transApp1/outbox"));
        dirsToClean.add(new File(BASE_PATH_MH, "transApp1/receipts"));

        dirsToClean.add(new File(BASE_PATH_MH, "transApp2/inbox"));
        dirsToClean.add(new File(BASE_PATH_MH, "transApp2/outbox"));
        dirsToClean.add(new File(BASE_PATH_MH, "transApp2/receipts"));

        dirsToClean.add(new File(INSTALL_DIR, "workingDir/tmp/receiving"));

        dirsToClean.addAll(addSedexDirectories(BASE_PATH_SDX));
        dirsToClean.addAll(addMHWorkingDirectories(INSTALL_DIR + "/workingDir"));

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

    public void testComplete() throws Exception {
        PropertyConfigurator.configureAndWatch(INSTALL_DIR + "/conf/log4j.properties");

        cleanDirectories(dirsToClean);
        initialize();
        validateBeforeRun();

        MessageHandlerService mhs = new MessageHandlerService();
        Integer result = mhs.start(new String[]{INSTALL_DIR + "/conf/config.xml"});
        Thread.sleep(15 * 1000);  //15 seconds
        result = mhs.stop(0);
        assertTrue(0 == result);

        validateAfterRun();
    }

    abstract void validateBeforeRun() throws Exception;

    abstract void validateAfterRun() throws Exception;

    /**
     * Initialize the data structure before test will run.
     *
     * @throws IOException
     */
    abstract void initialize() throws IOException;
}
