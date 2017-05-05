/*
 * $Id: ProtocolWriter.java 327 2014-01-27 13:07:13Z blaser $
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
 */
package ch.admin.suis.msghandler.log;

import java.io.*;

/**
 * Singleton which is used to create *.prot files for each processed file. Functionality can be enabled and disabled
 * with the config.xml file. Enable is same functionality as in version below 3.0.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 18.07.2012
 */
public final class ProtocolWriter {

	private static final ProtocolWriter INSTANCE = new ProtocolWriter();

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProtocolWriter.class.getName());

	private static final String SUFFIX_ERR = ".err";

	private static final String SUFFIX_PROT = ".prot";

	private boolean active = false;

	private ProtocolWriter() {
	}

	public static ProtocolWriter getInstance() {
		return INSTANCE;
	}

	/**
	 * Writes the given text to the protocol file. If this functionality is enabled in the config.xml section:
	 * <database ...produceProtocol="{true,false}"... />
	 *
	 * @param text     The text to write.
	 * @param toDir    The directory where to write...
	 * @param filename The filename
	 */
	public void writeProtocol(File toDir, String filename, final String text) {
		if (!active) {
			return;
		}

		File protFile = getProtocolFile(toDir, filename);
		if (write(protFile, text)) {
			LOG.debug("protocol file written: " + protFile.getAbsolutePath());
		}
	}

	/**
	 * Writes the given text to the protocol file. If this functionality is enabled in the config.xml section:
	 * <database ...produceProtocol="{true,false}"... />
	 *
	 * @param text     The text to write.
	 * @param toDir    The directory where to write...
	 * @param filename The filename
	 */
	public void writeProtocolError(File toDir, String filename, final String text) {
		if (!active) {
			return;
		}

		File protFile = getErrorProtocolFile(toDir, filename);
		if (write(protFile, text)) {
			LOG.debug("error file written: " + protFile.getAbsolutePath());
		}
	}

	private boolean write(File file, String text) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
			writer.write(text);
			return true;
		} catch (IOException ex) {
			LOG.fatal("cannot write protocol file: " + file.getAbsolutePath() + " : " + ex.getMessage(), ex);
		}
		return false;
	}

	/**
	 * Returns the pointer to the protocol file corresponding to the given file in the provided directory.
	 *
	 * @param toDir    where the protocol file should be created
	 * @param filename the name of the file for which the protocol is created
	 * @return the protocol file
	 */
	private File getProtocolFile(File toDir, String filename) {
		return new File(toDir, filename + SUFFIX_PROT);
	}

	/**
	 * Returns the pointer to the error protocol file corresponding to the given file in the provided directory.
	 *
	 * @param toDir    where the protocol file should be created
	 * @param filename the name of the file for which the protocol is created
	 * @return the protocol file
	 */
	private File getErrorProtocolFile(File toDir, String filename) {
		return new File(toDir, filename + SUFFIX_ERR);
	}

	/**
	 * If true, this will produce a *.prot file for each file. Same functionality as in version prior to 3.0. <br /> Default
	 * value is false.
	 *
	 * @return Whether the protocol writer is active or not. True if it is (see config.xml), false if it is not.
	 */
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}