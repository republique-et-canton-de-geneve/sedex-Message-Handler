/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.admin.suis.msghandler.log;

import ch.admin.suis.msghandler.monitor.FileNameFilter;
import ch.admin.suis.msghandler.monitor.FilterClient;
import ch.admin.suis.msghandler.monitor.MonitorException;
import ch.admin.suis.msghandler.monitor.ParticipantIdFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author kb
 */
public class DbLogServiceTest extends TestCase {

    private DbLogService logService;

    public DbLogServiceTest(String testName) {
        super(testName);
    }

    private File tmpDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        tmpDir = File.createTempFile("mh-log-db", "");
        assertTrue(tmpDir.delete());
        assertTrue(tmpDir.mkdir());
        tmpDir.deleteOnExit();

        logService = new DbLogService();
        logService.setBase(tmpDir.getAbsolutePath());
        logService.setMaxAge(2);
        logService.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        logService.destroy();
        for(File file : tmpDir.listFiles()){
            file.delete();
        }
    }

    public void testGetByFileName() throws LogServiceException, MonitorException {
        logService.setSending(Mode.MH, "participantId1", "file1");
        logService.setSending(Mode.MH, "participantId11", "file1");
        logService.setForwarded(Mode.MH, "participantId1", "file1", "msgId1");
        logService.setSending(Mode.MH, "participantId1", "file1");

        logService.setSending(Mode.MH, "participantId2", "file2");

        List<DBLogEntry> logEntries = logService.getAllEntries();
        assertEquals(3, logEntries.size());

        FilterClient fc1 = new FilterClient();
        fc1.addFilter(new FileNameFilter("file1"));
        assertEquals(2, fc1.filter(logEntries).size());

        FilterClient fc2 = new FilterClient();
        fc2.addFilter(new FileNameFilter("file2"));
        assertEquals(1, fc2.filter(logEntries).size());

        FilterClient fc3 = new FilterClient();
        fc3.addFilter(new FileNameFilter("file"));
        assertEquals(3, fc3.filter(logEntries).size());

        FilterClient fc4 = new FilterClient();
        fc4.addFilter(new FileNameFilter("fiLE"));
        assertEquals(3, fc3.filter(logEntries).size());
    }

    public void testGetByParticipantId() throws LogServiceException, MonitorException {
        logService.setSending(Mode.MH, "T1-7-4", "f1");
        logService.setSending(Mode.MH, "T1-7-4", "f1");
        logService.setForwarded(Mode.MH, "T1-7-4", "f1", "msgId1");
        logService.setSending(Mode.MH, "T1-7-4", "f1");

        logService.setSending(Mode.MH, "T1-3-4", "f2");

        List<DBLogEntry> logEntries = logService.getAllEntries();
        assertEquals(2, logEntries.size());

        FilterClient fc1 = new FilterClient();
        fc1.addFilter(new ParticipantIdFilter("T1-7-4"));
        assertEquals(1, fc1.filter(logEntries).size());

        FilterClient fc2 = new FilterClient();
        fc2.addFilter(new ParticipantIdFilter("T1-3-4"));
        assertEquals(1, fc2.filter(logEntries).size());

        FilterClient fc3 = new FilterClient();
        fc3.addFilter(new ParticipantIdFilter("T1-"));
        assertEquals(0, fc3.filter(logEntries).size());

        FilterClient fc4 = new FilterClient();
        fc4.addFilter(new ParticipantIdFilter("t1-7-4"));
        assertEquals(1, fc4.filter(logEntries).size());
    }
}
