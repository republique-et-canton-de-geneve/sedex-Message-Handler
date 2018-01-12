/*
 * $Id: LogServiceConfiguration.java 327 2014-01-27 13:07:13Z blaser $
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

import java.text.MessageFormat;

/**
 * The <code>LogServiceConfiguration</code> describes the configuration values
 * of the service that stores the status of the sent messages
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class LogServiceConfiguration {

	private String logBase;
	private int maxAge;
	private boolean resend;

	/**
	 * Returns the path location, where the log service will store its data.
	 *
	 * @return Returns the logBase.
	 */
	public String getLogBase() {
		return logBase;
	}

	/**
	 * @param logBase The logBase to set.
	 */
	public void setLogBase(String logBase) {
		this.logBase = logBase;
	}

	/**
	 * Returns the number of days, the log database entries are held in the
	 * DB log file before they are deleted.
	 *
	 * @return Returns the maxAge.
	 */
	public int getMaxAge() {
		return maxAge;
	}

	/**
	 * @param maxAge The maxAge to set.
	 */
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	/**
	 * @param resend boolean, whether the files have the right to be resent
	 */
	public void setResend(boolean resend) {
		this.resend = resend;
	}

	/**
	 * @return <code>true</code> if the configuration allows the
	 * files to be resent and <code>false</code> otherwise.
	 */
	public boolean getResend() {
		return this.resend;
	}

	@Override
	public String toString() {
		return MessageFormat.format("database folder: {0}; maxAge: {1}; resend: {2};",
				logBase,
				maxAge,
				resend
		);
	}

}
