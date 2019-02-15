/*
 * $Id: NamingService.java 327 2014-01-27 13:07:13Z blaser $
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
 *
 */

package ch.admin.suis.msghandler.naming;

/**
 * Interface to resolve name (of different sort) as based on the provided
 * parameter object.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public interface NamingService {

	/**
	 * Implementation of the naming service that does nothing and returns <code>null</code>.
	 */
	NamingService VOID = new NamingService() {

		@Override
		public String resolve(Object parameter) {
			return null;
		}
	};

	/**
	 * Returns a name (e.g. a participant ID) for the parameter object. If such a
	 * resolution is not possible, this method returns <code>null</code>.
	 *
	 * @param parameter parameter object
	 * @return String : participant ID
	 */
	String resolve(Object parameter);
}
