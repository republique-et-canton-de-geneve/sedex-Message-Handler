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
 * $Id: FilterTest.java 327 2014-01-27 13:07:13Z blaser $
 */

package ch.admin.suis.msghandler.monitor;

import ch.admin.suis.msghandler.log.DBLogEntry;
import ch.admin.suis.msghandler.log.LogStatus;
import ch.admin.suis.msghandler.log.Mode;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author kb
 */
public abstract class FilterTest extends TestCase {

    private final List<DBLogEntry> logEntries;

    public FilterTest(String testName) {
        super(testName);
        logEntries = new ArrayList<DBLogEntry>();

        DBLogEntry entry1 = new DBLogEntry();
        entry1.setFilename("file1.pdf");
        entry1.setState(LogStatus.SENT);
        entry1.setMessageId("id1");
        entry1.setMode(Mode.MH);
        entry1.setRecipientId("T1-2-3");
        entry1.setReceivedDate("2012-07-19");
        entry1.setSentDate("2012-07-19");

        DBLogEntry entry2 = new DBLogEntry();
        entry2.setFilename("file2.pdf");
        entry2.setState(LogStatus.FORWARDED);
        entry2.setMessageId("id2");
        entry2.setMode(Mode.MH);
        entry2.setRecipientId("T1-2-3");

        DBLogEntry entry3 = new DBLogEntry();
        entry3.setFilename("file3.pdf");
        entry3.setState(LogStatus.FORWARDED);
        entry3.setMessageId("id3");
        entry3.setMode(Mode.MH);
        entry3.setRecipientId("T4-4-4");
        entry3.setSentDate("2012-07-15");

        logEntries.add(entry1);
        logEntries.add(entry2);
        logEntries.add(entry3);

    }

    public List<DBLogEntry> getLogEntries() {
        return logEntries;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
}
