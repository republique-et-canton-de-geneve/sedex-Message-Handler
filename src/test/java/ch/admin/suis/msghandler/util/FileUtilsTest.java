/*
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
 * $Id$
 */
package ch.admin.suis.msghandler.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 *
 * @author kb
 */
public class FileUtilsTest {

	@Test
	public void testGetFilename() throws IOException {
		File tmpDir = File.createTempFile("mh-test", "");
		assertTrue(tmpDir.delete());
		assertTrue(tmpDir.mkdir());
		tmpDir.deleteOnExit();

		File file1 = new File(tmpDir, "file1");
		assertTrue(file1.createNewFile());

		String fileName = FileUtils.getFilename(tmpDir, "file1");
		File newFile = new File(fileName);
		assertTrue(newFile.getName().startsWith("file1-"));
		// assertTrue(newFile.getName().endsWith("-1"));
		assertEquals(3, newFile.getName().split("-").length);
		assertTrue(true);

		fileName = FileUtils.getFilename(tmpDir, "file1.pdf");
		newFile = new File(fileName);
		assertEquals("file1.pdf", newFile.getName());

		file1 = new File(tmpDir, "file1.pdf");
		assertTrue(file1.createNewFile());
		fileName = FileUtils.getFilename(tmpDir, "file1.pdf");
		newFile = new File(fileName);
		assertTrue(newFile.getName().startsWith("file1-"));
		assertTrue(newFile.getName().endsWith(".pdf"));
		assertEquals(3, newFile.getName().split("-").length);
	}

	@Test
	public void testCreatePath() {
		String parent = null;
		String child = "outbox1";
		File result = FileUtils.createPath(parent, child);
		assertTrue(result.isAbsolute());

		parent = "";
		result = FileUtils.createPath(parent, child);
		assertTrue(result.isAbsolute());

		parent = "parent";
		result = FileUtils.createPath(parent, child);
		assertTrue(result.getAbsolutePath()
				.endsWith("parent" + File.separator + "outbox1"));
		assertTrue(result.isAbsolute());

		// These test only if under Linux
		if (!System.getProperty("os.name").startsWith("Windows")) {
			parent = null;
			child = "/outbox1"; // test absolute path
			result = FileUtils.createPath(parent, child);
			assertEquals(child, result.getAbsolutePath());
			assertTrue(result.isAbsolute());

			parent = "";
			child = "/outbox1"; // test absolute path
			result = FileUtils.createPath(parent, child);
			assertEquals(child, result.getAbsolutePath());
			assertTrue(result.isAbsolute());

			parent = "/parent";
			child = "/outbox1"; // test absolute path
			result = FileUtils.createPath(parent, child);
			assertEquals(child, result.getAbsolutePath());
			assertTrue(result.isAbsolute());

		}
	}

	@Test
	public void testFreeDiskSpace() {
		File file1 = new File("./");
		long freeDiskSpace1 = FileUtils.getFreeDiskSpace(file1);
		assertTrue(freeDiskSpace1 > 0);

		File file2 = new File("./notExistFile.txt");
		long freeDiskSpace2 = FileUtils.getFreeDiskSpace(file2);
		assertTrue(freeDiskSpace2 > 0);
		//assertEquals(freeDiskSpace1, freeDiskSpace2); // This may fail if a process
																									// writes something to the
																									// fs...

		File file3 = new File("./notExistDir/notExistFile.txt");
		long freeDiskSpace3 = FileUtils.getFreeDiskSpace(file3);
		assertEquals(-1, freeDiskSpace3);
	}
}
