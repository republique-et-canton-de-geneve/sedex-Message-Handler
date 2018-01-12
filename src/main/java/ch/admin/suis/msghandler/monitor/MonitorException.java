/*
 * $Id: MonitorException.java 327 2014-01-27 13:07:13Z blaser $
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

/**
 * MonitorException. This is used to handle exceptions which may occur during handle HTTP Get requests in the
 * MonitorServlet.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 18.07.2012
 */
public class MonitorException extends Exception {

	/**
	 * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
	 *                method.
	 */
	public MonitorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null :
	 * cause.toString())</tt> (which typically contains the class and detail message of <tt>cause</tt>). This constructor
	 * is useful for exceptions that are little more than wrappers for other throwables (for example, {@link
	 * java.security.PrivilegedActionException}).
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or
	 *              unknown.)
	 */
	public MonitorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause. <p>Note that the detail message associated
	 * with
	 * <code>cause</code> is <i>not</i> automatically incorporated in this exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 */
	public MonitorException(String message, Throwable cause) {
		super(message, cause);
	}
}