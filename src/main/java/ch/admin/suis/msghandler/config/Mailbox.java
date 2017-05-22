/*
 * $Id: Mailbox.java 327 2014-01-27 13:07:13Z blaser $
 *
 * Copyright (C) 2008-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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

package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.configuration.ConfigurationException;

import java.io.File;

/**
 * Base class for the in- and outboxes.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public abstract class Mailbox {

	private final File directory;

	/**
	 * Constructor for a new Mailbox.
	 *
	 * @param directory of the mailbox
	 */
	public Mailbox(File directory) throws ConfigurationException {
		this.directory = directory;
		FileUtils.isDirectory(directory, "Mailbox");
	}

	/**
	 * @return Returns the path from this Mailbox.
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * @return Returns the name of this Mailbox.
	 */
	public String getName() {
		return directory.getName();
	}
}
