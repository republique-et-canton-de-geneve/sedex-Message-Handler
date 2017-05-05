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
 * $Id: SignerTest.java 327 2014-01-27 13:07:13Z blaser $
 */

package ch.admin.suis.msghandler.signer;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.config.SigningOutbox;
import ch.admin.suis.msghandler.config.SigningOutboxMHCfg;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Security;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assume;
import org.junit.Ignore;

/**
 * @author kb
 */
public class SignerTest extends TestCase {

    private static final String BASE_PATH = "./src/test/resources/sign";

    private final File p12File = new File(BASE_PATH, "thawtetest.p12");

    //  private final File pdfToSign = new File(BASE_PATH, "pdfB.pdf");
    private final File signatureProperties = new File(BASE_PATH, "signature.properties");

    public SignerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    protected void tearDown() throws Exception {
        clean();
        super.tearDown();
    }

    private void clean() {
        for (File file : getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox1"))) {
            file.delete();
        }

        for (File file : getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox2"))) {
            file.delete();
        }

        for (File file : getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox1Processed"))) {
            file.delete();
        }
    }

    private void initialize() throws IOException {
        clean();
        List<File> files = getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox1Files"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH + "/signingOutbox1", f.getName()));
        }

        files = getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox2Files"));
        for (File f : files) {
            FileUtils.copyFile(f, new File(BASE_PATH + "/signingOutbox2", f.getName()));
        }
    }

    /**
     * Test of sign method, of class Signer.
     */
    public void testSignBasic() throws SignerException, IOException, ConfigurationException {
        initialize();
        System.out.println("testSignBasic");
        SigningOutbox signOutbox = new SigningOutboxMHCfg(p12File, "12345678", new File(BASE_PATH + "/" + "signingOutbox1"),
                signatureProperties, null);
        Signer signer = new Signer(signOutbox, createWorkingDir());

        List<File> signedFiles = signer.sign();
        for (File f : signedFiles) {
            f.deleteOnExit();
        }
        assertEquals(signOutbox.getAllPDFsToSign().size(), signedFiles.size());
        assertEquals(signedFiles, signOutbox.getAllPDFsToSign());
        signer.cleanUp(signedFiles);
        assertEquals(0, signOutbox.getAllPDFsToSign().size());

    }

    /**
     * Test of sign method, of class Signer.
     */
    public void testSignList() throws SignerException, IOException, ConfigurationException {
        initialize();
        System.out.println("testSignList");
        SigningOutbox signOutbox1 = new SigningOutboxMHCfg(p12File, "12345678", new File(BASE_PATH + "/" + "signingOutbox1"),
                signatureProperties, null);
        SigningOutbox signOutbox2 = new SigningOutboxMHCfg(p12File, "12345678", new File(BASE_PATH + "/" + "signingOutbox2"),
                signatureProperties, null);

        List<SigningOutbox> outboxes = new ArrayList<SigningOutbox>();
        outboxes.add(signOutbox1);
        outboxes.add(signOutbox2);

        Signer signer = new Signer(outboxes, createWorkingDir());

        List<File> signedFiles = signer.sign();
        for (File f : signedFiles) {
            f.deleteOnExit();
        }
        assertEquals(signOutbox1.getAllPDFsToSign().size() + signOutbox2.getAllPDFsToSign().size(), signedFiles.size());

        signer.cleanUp(signedFiles);
        assertEquals(0, signOutbox1.getAllPDFsToSign().size());
        signer.cleanUp(signedFiles);
        assertEquals(0, signOutbox2.getAllPDFsToSign().size());

    }

    /**
     * Test of sign method, of class Signer.
     */
    public void testSignProcessedDir() throws SignerException, IOException, ConfigurationException {
        initialize();
        System.out.println("testSignProcessedDir");
        SigningOutbox signOutbox = new SigningOutboxMHCfg(p12File, "12345678", new File(BASE_PATH + "/" + "signingOutbox1"),
                signatureProperties, new File(BASE_PATH + "/" + "signingOutbox1Processed"));
        Signer signer = new Signer(signOutbox, createWorkingDir());

        int numberOfFiles = signOutbox.getAllPDFsToSign().size();

        List<File> signedFiles = signer.sign();
        for (File f : signedFiles) {
            f.deleteOnExit();
        }
        assertEquals(numberOfFiles, signedFiles.size());

        signer.cleanUp(signedFiles);
        assertEquals(0, signOutbox.getAllPDFsToSign().size()); //now they should be moved

        assertEquals(numberOfFiles, getAllFilesFromDir(signOutbox.getProcessedDir()).size());

    }

    private List<File> getAllFilesFromDir(File directory) {
        if (directory == null) {
            return new ArrayList<File>();
        }

        File[] files = ch.admin.suis.msghandler.util.FileUtils.listFiles(directory, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && ch.admin.suis.msghandler.util.FileUtils.canRead(pathname) && !pathname.
                        isHidden();
            }
        });

        List<File> retVal = new ArrayList();
        Collections.addAll(retVal, files);
        return retVal;
    }

    private File createWorkingDir() throws IOException {
        File workingDir = File.createTempFile("mh_tmpSigned", null);
        workingDir.delete();
        workingDir.mkdir();
        workingDir.deleteOnExit();
        return workingDir;
    }
}
