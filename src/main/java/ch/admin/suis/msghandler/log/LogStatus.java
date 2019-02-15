/*
 * $Id: LogStatus.java 327 2014-01-27 13:07:13Z blaser $
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
 * Enumeration for the status of the sent or to-be-sent files.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public enum LogStatus {

	/**
	 * the message is being sent, the files are taken from the outbox and put into a ZIP file.
	 */
	SENDING(1),
	/**
	 * the message is successfully sent, the ZIP file is transfered to the outbox of the Sedex adapter, but not yet
	 * delivered or expired (there is no confirmation receipt)
	 */
	SENT(2),
	/**
	 * the message is taken and sent by the Sedex adapter, but there is no confiramtion receipt yet
	 */
	FORWARDED(3),
	/**
	 * the message is received by the recipeient; there is the receipt in the xml directory of the Sedex adapter
	 */
	DELIVERED(4),
	/**
	 * the message was not delivered and is expired; there is the receipt in the xml directory of the Sedex adapter
	 */
	EXPIRED(5),
	/**
	 * an error is reported by the Sedex adapter; there is the receipt in the xml directory
	 */
	ERROR(8);

	private int code;

	private LogStatus(int code) {
		this.code = code;
	}

	/**
	 * @return Returns the code.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Converts an int to LogStatus enum.
	 *
	 * @param code a valid code
	 * @return LogStatus. If code is not known LogStatus.ERROR will be returned
	 */
	public static LogStatus fromInt(int code) {
		switch (code) {
			case 1:
				return SENDING;
			case 2:
				return SENT;
			case 3:
				return FORWARDED;
			case 4:
				return DELIVERED;
			case 5:
				return EXPIRED;
			case 8:
			default:
				return ERROR;
		}
	}
}
