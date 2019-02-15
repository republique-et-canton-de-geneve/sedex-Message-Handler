/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.admin.suis.msghandler.log;

import ch.admin.suis.msghandler.common.CompleteBasicTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author kb
 */
public class ProtocolWriterTest extends CompleteBasicTest {

    public ProtocolWriterTest(String testName) {
        super(testName);
    }

    /**
     * Test of writeProtocol method, of class ProtocolWriter.
     */
    public void testFull() throws IOException {
        ProtocolWriter pw = ProtocolWriter.getInstance();
        assertTrue(pw != null); //need instance
        assertEquals(pw, ProtocolWriter.getInstance()); //has to be the same
        pw.setActive(true);
        assertTrue(ProtocolWriter.getInstance().isActive()); //Singleton

        pw.setActive(false); //No files should be writen! Disabled
        File directory = File.createTempFile("MH_protocolWriterTest", "");
        directory.delete();
        directory.mkdir();
        directory.deleteOnExit();

        pw.writeProtocol(directory, "file.pdf", "Just some text");
        pw.writeProtocolError(directory, "file.pdf", "Just some other text");

        assertTrue(getAllFilesFromDir(directory).isEmpty());

        pw.setActive(true); //Now the files should be writen! Enabled
        pw.writeProtocol(directory, "file.pdf", "Just some text");
        pw.writeProtocolError(directory, "file.pdf", "Just some other text");
        List<File> files = getAllFilesFromDir(directory);
        assertEquals(2, files.size());
        boolean protFileAvailable = false;
        boolean errFileAvailable = false;
        for (File f : files) {
            if (f.getName().equals("file.pdf.prot")) {
                protFileAvailable = true;
            } else if (f.getName().equals("file.pdf.err")) {
                errFileAvailable = true;
            }
            f.delete();
        }
        assertTrue(errFileAvailable);
        assertTrue(protFileAvailable);
    }
}
