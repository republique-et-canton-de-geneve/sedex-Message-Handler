/*
 * $Id: CommandInterfaceConfiguration.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.servlet;

import ch.admin.suis.msghandler.common.ClientCommons;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * Describes the configuration of the command-line interface to the service.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class CommandInterfaceConfiguration {

	private String host;

	private int port;

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String toString() {
		return MessageFormat.format("host name: {0}; port number: {1}",
				StringUtils.defaultIfEmpty(host, ClientCommons.NOT_SPECIFIED),
				port == 0 ? ClientCommons.NOT_SPECIFIED : port);
	}
}
