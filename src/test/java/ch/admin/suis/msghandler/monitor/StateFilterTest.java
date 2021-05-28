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
 * $Id$
 */
package ch.admin.suis.msghandler.monitor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.admin.suis.msghandler.log.DBLogEntry;
import ch.admin.suis.msghandler.log.LogStatus;

/**
 *
 * @author kb
 */
public class StateFilterTest extends FilterTest {


	@Test
  public void testEventFilter() throws MonitorException {
    FilterClient fc = new FilterClient();
    fc.addFilter(new StateFilter(LogStatus.SENT));
    List<DBLogEntry> filtered = fc.filter(getLogEntries());
    assertEquals(1, filtered.size());
  }
}
