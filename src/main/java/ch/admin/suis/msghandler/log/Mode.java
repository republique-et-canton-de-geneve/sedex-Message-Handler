/*
 * $Id: Mode.java 327 2014-01-27 13:07:13Z blaser $
 *
 * Copyright (C) 2009-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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
 * The source of the messages in the internal log DB.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public enum Mode {

	/**
	 * message handler
	 */
	MH(1),
	/**
	 * some other application (in transparent mode)
	 */
	TRANSP(2);

	private int code;

	private Mode(int code) {
		this.code = code;
	}

	/**
	 * @return Returns the code.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Converts an int to Mode enum.
	 *
	 * @param code a valid code
	 * @return Mode. If code is not known Mode.TRANSP will be returned
	 */
	public static Mode fromInt(int code) {
		switch (code) {
			case 1:
				return MH;
			case 2:
			default:
				return TRANSP;
		}
	}
}
