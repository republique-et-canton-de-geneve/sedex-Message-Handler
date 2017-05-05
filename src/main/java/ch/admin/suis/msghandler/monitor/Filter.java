/*
 * $Id: Filter.java 327 2014-01-27 13:07:13Z blaser $
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

import java.util.ArrayList;
import java.util.List;

/**
 * Filters a List of DBLogEntries on different criteria.<br /> It's a kind of Chain-of-responsibility GoF Pattern.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 20.07.2012
 */
public abstract class Filter {

	private Filter nextFilter = null;

	/**
	 * Adds a filter to the end of the chain.
	 *
	 * @param filter The filter
	 */
	void addFilter(Filter filter) {
		if (nextFilter == null) {
			nextFilter = filter;
		} else {
			nextFilter.addFilter(filter);
		}
	}

	/**
	 * Filter the given list. Returns a list which only contains the filtered (accepted) elements.
	 *
	 * @param entries list to filter
	 * @return filtered list
	 * @throws MonitorException if there should happen an exception during the filter process
	 */
	List<DBLogEntry> filter(List<DBLogEntry> entries) throws MonitorException {
		try {
			List<DBLogEntry> filteredEntries = new ArrayList<>();
			for (DBLogEntry logEntry : entries) {
				if (filter(logEntry)) {
					filteredEntries.add(logEntry);
				}
			}

			if (nextFilter != null) {
				return nextFilter.filter(filteredEntries);
			} else {
				return filteredEntries;
			}
		} catch (RuntimeException ex) {
			throw new MonitorException(ex);
		}
	}

	/**
	 * Real filter implementation. Return true if the entry match the filter otherwise false.
	 *
	 * @param entry A DBLogEntry
	 * @return boolean, true if the entry match the filter otherwise false.
	 */
	protected abstract boolean filter(DBLogEntry entry);
}