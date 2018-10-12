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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Ignore;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.config.SigningOutbox;
import ch.admin.suis.msghandler.config.SigningOutboxMHCfg;
import junit.framework.TestCase;

/**
 * @author kb
 */
@Ignore // TODO : SWITCH THIS TEST ON ONCE IC-557 IS FIXED
public class SignerTest extends TestCase {

    private static final String BASE_PATH = "./src/test/resources/sign";
    private static final String BASE_PATH_MSGHANDLER_64 = "./src/test/resources/sign-MSGHANDLER-64";

    private final File p12File = new File(BASE_PATH, "thawtetest.p12");

    // private final File pdfToSign = new File(BASE_PATH, "pdfB.pdf");
    private final File signatureProperties = new File(BASE_PATH, "signature.properties");

    // Temporäre Verzeichnisse die erstellt und wieder gelöscht werden
    private final File signingOutbox1 = new File(BASE_PATH + "/signingOutbox1");
    private final File signingOutbox1Processed = new File(BASE_PATH + "/signingOutbox1Processed");
    private final File signingOutbox2 = new File(BASE_PATH + "/signingOutbox2");

    public SignerTest(String testName) {
	super(testName);
    }

    private void createDirectory(File theDirToCreate) throws SecurityException {
	// if the directory does not exist, create it
	if (!theDirToCreate.exists()) {
	    theDirToCreate.mkdir();
	}
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();
	// Für Zeile unter siehe
	// https://golb.hplar.ch/p/JCE-policy-changes-in-Java-SE-8u151-and-8u152
	Security.setProperty("crypto.policy", "unlimited");
	Security.addProvider(new BouncyCastleProvider());

	// Erstelle die für die Tests erforderlichen Verzeichnisse
	createDirectory(signingOutbox1);
	createDirectory(signingOutbox2);
	createDirectory(signingOutbox1Processed);
    }

    @Override
    protected void tearDown() throws Exception {
	Security.removeProvider("BC");

	// Lösche die für die Tests erforderlichen Verzeichnisse rekursiv
	FileUtils.deleteDirectory(signingOutbox1);
	FileUtils.deleteDirectory(signingOutbox2);
	FileUtils.deleteDirectory(signingOutbox1Processed);

	super.tearDown();
    }

    private void initialize() throws IOException {
	cleanOutboxes();
	populateSigningOutboxes1and2();
    }

    private void cleanOutboxes() throws IOException {
	FileUtils.cleanDirectory(signingOutbox1);
	FileUtils.cleanDirectory(signingOutbox2);
	FileUtils.cleanDirectory(signingOutbox1Processed);
    }

    private void populateSigningOutboxes1and2() throws IOException {
	List<File> files = getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox1Files"));
	for (File f : files) {
	    FileUtils.copyFile(f, new File(signingOutbox1, f.getName()));
	}

	files = getAllFilesFromDir(new File(BASE_PATH + "/signingOutbox2Files"));
	for (File f : files) {
	    FileUtils.copyFile(f, new File(signingOutbox2, f.getName()));
	}
    }

    public void testSigningWithOneOutbox() throws SignerException, IOException, ConfigurationException {
	    initialize();
	    System.out.println("testSigningWithOneOutbox");
	    SigningOutbox signOutbox = new SigningOutboxMHCfg(p12File, "12345678", signingOutbox1,
	            signatureProperties, null);
	    final File workingDir = createWorkingDir();
	    final File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    Signer signer = new Signer(signOutbox, workingDir, corruptedDir);

	    List<File> signedFiles = signer.sign();
	    signedFiles.forEach((f) -> {f.deleteOnExit();});

	    assertEquals(signOutbox.getAllPDFsToSign().size(), signedFiles.size());
	    assertEquals(signedFiles, signOutbox.getAllPDFsToSign());
	    signer.cleanUp(signedFiles);
	    assertEquals(0, signOutbox.getAllPDFsToSign().size());

	    // The corrupted directory must be empty
	    assert FileUtils.listFiles(corruptedDir, TrueFileFilter.INSTANCE, null).isEmpty();
	  }

	  // Testcase for ticket MSGHANDLER-64
	  public void testSigningWithOneOutbox_MSGHANDLER_64() throws SignerException, IOException, ConfigurationException
	  {
	    cleanOutboxes();

	    // Populate signing inbox with PDF files. There a 3 files with extension .pdf there, but one of them is not a PDF
	    // file!
	    List<File> files = getAllFilesFromDir(new File(BASE_PATH_MSGHANDLER_64));
	    for (File f : files)
	    {
	      FileUtils.copyFile(f, new File(signingOutbox1, f.getName()));
	    }

	    System.out.println("testSigningWithOneOutbox_MSGHANDLER_64");
	    final File workingDir = createWorkingDir();
	    final File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    SigningOutbox signOutbox = new SigningOutboxMHCfg(p12File, "12345678", signingOutbox1, signatureProperties, null);
	    Signer signer = new Signer(signOutbox, workingDir, corruptedDir);

	    List<File> signedFiles = signer.sign();
	    signedFiles.forEach((f) -> { f.deleteOnExit(); });

	    assertEquals("We must have two signed files", 2, signedFiles.size());
	    assertEquals("The corrupted directory must contain one file",
	        1, FileUtils.listFiles(corruptedDir, TrueFileFilter.INSTANCE, null).size());
	    File foundFile = FileUtils.listFiles(corruptedDir, TrueFileFilter.INSTANCE, null).iterator().next();
	    assertEquals("The file in the corrupted dir must be 'pdfA-not_a_pdf_file.pdf'",
	        "pdfA-not_a_pdf_file.pdf", foundFile.getName());
	    signer.cleanUp(signedFiles);
	  }

	  public void testSigningWithTwoOutboxes() throws SignerException, IOException, ConfigurationException {
	    initialize();
	    System.out.println("testSigningWithTwoOutboxes");
	    SigningOutbox signOutbox1 = new SigningOutboxMHCfg(p12File, "12345678", signingOutbox1,
	            signatureProperties, null);
	    SigningOutbox signOutbox2 = new SigningOutboxMHCfg(p12File, "12345678", signingOutbox2,
	            signatureProperties, null);

	    List<SigningOutbox> outboxes = Arrays.asList(signOutbox1, signOutbox2);

	    final File workingDir = createWorkingDir();
	    final File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    Signer signer = new Signer(outboxes, workingDir, corruptedDir);

	    List<File> signedFiles = signer.sign();
	    signedFiles.forEach((f) -> {f.deleteOnExit();});
	    assertEquals(signOutbox1.getAllPDFsToSign().size() + signOutbox2.getAllPDFsToSign().size(), signedFiles.size());

	    signer.cleanUp(signedFiles);
	    assertEquals(0, signOutbox1.getAllPDFsToSign().size());
	    signer.cleanUp(signedFiles);
	    assertEquals(0, signOutbox2.getAllPDFsToSign().size());

	    // The corrupted directory must be empty
	    assert FileUtils.listFiles(corruptedDir, TrueFileFilter.INSTANCE, null).isEmpty();
	  }

	  /**
	   * Test of sign method, of class Signer.
	   */
	  public void testSigningWithOneOutboxAndAProcessedDirectory() throws SignerException, IOException, ConfigurationException {
	    initialize();
	    System.out.println("testSigningWithOneOutboxAndAProcessedDirectory");
	    SigningOutbox signOutbox = new SigningOutboxMHCfg(p12File, "12345678", signingOutbox1,
	        signatureProperties, signingOutbox1Processed);
	    final File workingDir = createWorkingDir();
	    final File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    Signer signer = new Signer(signOutbox, workingDir, corruptedDir);

	    int numberOfFiles = signOutbox.getAllPDFsToSign().size();

	    List<File> signedFiles = signer.sign();
	    signedFiles.forEach((f) -> {f.deleteOnExit();});
	    assertEquals(numberOfFiles, signedFiles.size());

	    signer.cleanUp(signedFiles);
	    assertEquals(0, signOutbox.getAllPDFsToSign().size()); //now they should be moved

	    assertEquals(numberOfFiles, getAllFilesFromDir(signOutbox.getProcessedDir()).size());
	    // The corrupted directory must be empty
	    assert FileUtils.listFiles(corruptedDir, TrueFileFilter.INSTANCE, null).isEmpty();
	  }

	  private List<File> getAllFilesFromDir(File directory) {
	    if(directory == null) {
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
	    final File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    workingDir.mkdir();
	    corruptedDir.mkdir();
	    workingDir.deleteOnExit();
	    return workingDir;
	  }
}
