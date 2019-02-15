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
 * $Id: UntilFilterTest.java 327 2014-01-27 13:07:13Z blaser $
 */

package ch.admin.suis.msghandler.monitor;

import ch.admin.suis.msghandler.log.DBLogEntry;
import ch.admin.suis.msghandler.util.DateUtils;

import java.util.List;

/**
 * @author kb
 */
public class UntilFilterTest extends FilterTest {

    public UntilFilterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testUntilFilter() throws MonitorException {
        FilterClient fc = new FilterClient();
        fc.addFilter(new UntilFilter(DateUtils.xsdDateTimeToDate("2012-07-18")));
        List<DBLogEntry> filtered = fc.filter(getLogEntries());
        assertEquals(1, filtered.size());

        fc = new FilterClient();
        fc.addFilter(new UntilFilter(DateUtils.xsdDateTimeToDate("2012-06-19")));
        filtered = fc.filter(getLogEntries());
        assertEquals(0, filtered.size());

        fc = new FilterClient();
        fc.addFilter(new UntilFilter(DateUtils.xsdDateTimeToDate("2013-06-19")));
        filtered = fc.filter(getLogEntries());
        assertEquals(2, filtered.size());
    }
}
