/*
 * $Id: LogServiceException.java 327 2014-01-27 13:07:13Z blaser $
 * 
 * Copyright (C) 2006 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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

package ch.admin.suis.msghandler.log;

/**
 * The <code>LogServiceException</code> is thrown by the log service
 * when some of the logging operations cannot be accomplished.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
@SuppressWarnings("serial")
public class LogServiceException extends Exception {

	/**
	 * Creates a new <code>LogServiceException</code> with the given message.
	 *
	 * @param message The message for the exception.
	 */
	public LogServiceException(String message) {
		super(message);
	}

	/**
	 * Creates a new <code>LogServiceException</code> caused by the supplied
	 * exception.
	 *
	 * @param e The parent Exception.
	 */
	public LogServiceException(Throwable e) {
		super(e);
	}

}
