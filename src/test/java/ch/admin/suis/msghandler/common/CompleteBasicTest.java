/*
 * $Id$
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
package ch.admin.suis.msghandler.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.admin.suis.msghandler.util.FileUtils;

/**
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 10.07.2012
 */
public abstract class CompleteBasicTest {

	/**
	 * Get all files from the given directory.
	 *
	 * @param directory
	 * @return
	 * @throws FileNotFoundException
	 */
	protected List<File> getAllFilesFromDir(File directory)
			throws FileNotFoundException {
		if (directory == null) {
			return new ArrayList<File>();
		}

		if (!directory.exists()) {
			throw new FileNotFoundException(
					"Directory not exist: " + directory.getAbsolutePath());
		}

		if (!directory.isDirectory()) {
			throw new RuntimeException(
					"It's a file. Has to be a directory: " + directory.getAbsolutePath());
		}

		File[] files = FileUtils.listFiles(directory, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory()
						&& ch.admin.suis.msghandler.util.FileUtils.canRead(pathname)
						&& !pathname.isHidden() && !pathname.getName().endsWith("~");
			}
		});

		List<File> retVal = new ArrayList<>();
		Collections.addAll(retVal, files);
		return retVal;
	}

	/**
	 * Deletes all files inside the given directories..
	 *
	 * @param directories
	 * @throws FileNotFoundException
	 */
	void cleanDirectories(List<File> directories) throws FileNotFoundException {
		for (File dirToClean : directories) {
			for (File deletableFile : getAllFilesFromDir(dirToClean)) {
				deletableFile.delete();
			}
		}
	}

	/**
	 * Gets a list of all sedex directories.
	 *
	 * @param sedexBase
	 *          sedex base directory which contians the sedex inbox, outbox, and
	 *          so on...
	 * @param directories
	 */
	List<File> addSedexDirectories(String sedexBase) {

		List<File> sedexDirs = new ArrayList<File>(4);

		sedexDirs.add(new File(sedexBase, "inbox"));
		sedexDirs.add(new File(sedexBase, "outbox"));
		sedexDirs.add(new File(sedexBase, "receipts"));
		sedexDirs.add(new File(sedexBase, "sent"));

		return sedexDirs;
	}

	/**
	 * Gets a list of all MH working directories.
	 *
	 * @param mhWorkingBase
	 * @param directories
	 */
	List<File> addMHWorkingDirectories(String mhWorkingBase) {

		List<File> sedexDirs = new ArrayList<File>(4);

		sedexDirs.add(new File(mhWorkingBase, "corrupted"));
		sedexDirs.add(new File(mhWorkingBase, "sent"));
		sedexDirs.add(new File(mhWorkingBase, "tmp/preparing"));
		sedexDirs.add(new File(mhWorkingBase, "tmp/receiving"));
		sedexDirs.add(new File(mhWorkingBase, "unknown"));
		return sedexDirs;
	}
}