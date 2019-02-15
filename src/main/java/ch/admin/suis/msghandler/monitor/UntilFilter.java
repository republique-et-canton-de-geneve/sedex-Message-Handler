/*
 * $Id: UntilFilter.java 327 2014-01-27 13:07:13Z blaser $
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
import ch.admin.suis.msghandler.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 20.07.2012
 */
public class UntilFilter extends Filter {

	private final Date until;

	/**
	 * Filter for a given date. Which means filter all Entries which are older than the "until" date.
	 *
	 * @param until the until date. Younger DBLogEntries will not pass the filter.
	 */
	public UntilFilter(Date until) {
		this.until = until;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	protected boolean filter(DBLogEntry entry) {
		List<Date> dates = new ArrayList<>(2);
		if (entry.getReceivedDate() != null) {
			dates.add(DateUtils.xsdDateTimeToDate(entry.getReceivedDate()));
		}
		if (entry.getSentDate() != null) {
			dates.add(DateUtils.xsdDateTimeToDate(entry.getSentDate()));
		}

		boolean insideUntil;

		for (Date d : dates) {
			insideUntil = until == null || d.before(until);
			if (insideUntil) {
				return true;
			}
		}
		return false;
	}
}