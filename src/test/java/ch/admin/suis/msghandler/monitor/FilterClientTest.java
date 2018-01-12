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
 * $Id: FilterClientTest.java 327 2014-01-27 13:07:13Z blaser $
 */

package ch.admin.suis.msghandler.monitor;

import ch.admin.suis.msghandler.log.LogStatus;
import ch.admin.suis.msghandler.util.DateUtils;

/**
 * @author kb
 */
public class FilterClientTest extends FilterTest {

    public FilterClientTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testComplete() throws MonitorException {
        FilterClient fc = new FilterClient();
        fc.addFilter(new FileNameFilter("file"));
        assertEquals(3, fc.filter(getLogEntries()).size());

        fc.addFilter(new StateFilter(LogStatus.FORWARDED));
        assertEquals(2, fc.filter(getLogEntries()).size());

        fc.addFilter(new ParticipantIdFilter("T4-4-4"));
        assertEquals(1, fc.filter(getLogEntries()).size());

        fc.addFilter(new FromFilter(DateUtils.xsdDateTimeToDate("2012-06-14")));
        assertEquals(1, fc.filter(getLogEntries()).size());

        fc.addFilter(new UntilFilter(DateUtils.xsdDateTimeToDate("2012-07-14")));
        assertEquals(0, fc.filter(getLogEntries()).size());
    }
}
