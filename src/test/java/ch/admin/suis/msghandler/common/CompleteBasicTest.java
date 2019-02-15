/*
 * $Id: CompleteBasicTest.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 10.07.2012
 */
public abstract class CompleteBasicTest extends TestCase {

	public CompleteBasicTest() {
	}

	public CompleteBasicTest(String testName) {
		super(testName);
	}

	/**
	 * Get all files from the given directory.
	 *
	 * @param directory
	 * @return
	 * @throws FileNotFoundException
	 */
	protected List<File> getAllFilesFromDir(File directory) throws FileNotFoundException {
		if (directory == null) {
			return new ArrayList<File>();
		}

		if (!directory.exists()) {
			throw new FileNotFoundException("Directory not exist: " + directory.getAbsolutePath());
		}

		if (!directory.isDirectory()) {
			throw new RuntimeException("It's a file. Has to be a directory: " + directory.getAbsolutePath());
		}

		DirectoryStream<Path> files = FileUtils.listFiles(directory, new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path path) throws IOException {
				File pathname = path.toFile();
				return !pathname.isDirectory() && ch.admin.suis.msghandler.util.FileUtils.canRead(pathname) && !pathname.
						isHidden() && !pathname.getName().endsWith("~");
			}
		});
		List<File> retVal = new ArrayList<>();
		for (Path path : files){
			retVal.add(path.toFile());
		}
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
	 * Hack to modify the classpath at runtime. Required for the groovy scripts. They'll be loaded with the class loader.
	 *
	 * @param s directory to add
	 * @throws Exception
	 */
	void addToClassPath(String s) throws Exception {
		File f = new File(s);
		URL u = f.toURI().toURL();
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class urlClass = URLClassLoader.class;
		Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[]{u});
	}

	/**
	 * Gets a list of all sedex directories.
	 *
	 * @param sedexBase   sedex base directory which contians the sedex inbox, outbox, and so on...
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
