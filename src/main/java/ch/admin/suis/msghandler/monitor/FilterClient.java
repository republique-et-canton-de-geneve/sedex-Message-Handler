/*
 * $Id$
 *
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
 */
package ch.admin.suis.msghandler.monitor;

import ch.admin.suis.msghandler.log.DBLogEntry;
import java.util.List;

/**
 * FilterClient is responsible to filter a List<DBLogEntry>.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 20.07.2012
 */
public class FilterClient {

  private Filter filter = null;

  /**
   * This adds a new Filter to the chain. The filters will be AND combined!
   *
   * @param filter
   */
  public void addFilter(Filter filter) {
    if(this.filter == null) {
      this.filter = filter;
    }
    else {
      this.filter.addFilter(filter);
    }
  }

  /**
   * Filters all DBLogEntries. Return value will be the DBLogEntries which matches all filters (AND combination).
   *
   * @param entries list to filter
   * @return filtered list
   * @throws MonitorException if there should happen an exception during the filter process
   */
  public List<DBLogEntry> filter(List<DBLogEntry> entries) throws MonitorException {
    return filter.filter(entries);
  }
}