/*
 * $Id: CompleteMultiReceiver1Test.java 327 2014-01-27 13:07:13Z blaser $
 *
 * Copyright 2013 by Swiss Federal Administration
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of the Swiss Federal Administration. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with the Swiss Federal Administration.
 */
package ch.admin.suis.msghandler.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Versand von einer Nachricht mit mehreren Empfängern. Es wird lokal eine native App und eine transparente App als
 * Empfänger sein. Weiter gibt es externe Empfänger. -> Sedex Outbox.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 26.02.2013
 */
public class CompleteMultiReceiver1Test extends CompleteMultiReceiverTest {

	@Override
	void validateBeforeRun() throws Exception {
	}

	@Override
	void validateAfterRun() throws Exception {

		checkNativeApp1();
		checkTransApp1();
		checkSedexOutbox();
		checkReceipts();
	}

	private void checkNativeApp1() throws FileNotFoundException {
		List<File> filesNativeApp1 = getAllFilesFromDir(new File(BASE_PATH_MH, "nativeApp1/inbox"));
		assertEquals(1, filesNativeApp1.size());
		assertEquals("test1.pdf", filesNativeApp1.get(0).getName());
	}

	private void checkTransApp1() throws FileNotFoundException {
		checkTransApp1(new File(BASE_PATH_MH, "transApp1/inbox"));
	}

	private void checkTransApp1(File path) throws FileNotFoundException {
		List<File> filesTransApp1 = getAllFilesFromDir(path);
		assertEquals(2, filesTransApp1.size());

		Set<String> fileNameSet = new HashSet<>();
		for (File file : filesTransApp1) {
			fileNameSet.add(file.getName());
		}

		assertTrue("envl_95b29548-8482-48d7-801d-1901fa8bf208.xml is missing", fileNameSet.contains(
				"envl_95b29548-8482-48d7-801d-1901fa8bf208.xml"));
		assertTrue("data_95b29548-8482-48d7-801d-1901fa8bf208.zip is missing", fileNameSet.contains(
				"data_95b29548-8482-48d7-801d-1901fa8bf208.zip"));
	}

	private void checkSedexOutbox() throws FileNotFoundException {
		checkTransApp1(new File(BASE_PATH_SDX, "/outbox"));
	}

	private void checkReceipts() throws FileNotFoundException {
		List<File> filesTransApp2 = getAllFilesFromDir(new File(BASE_PATH_MH, "transApp2/receipts"));
		assertEquals(2, filesTransApp2.size());

		Set<String> fileNameSet = new HashSet<>();
		for (File file : filesTransApp2) {
			fileNameSet.add(file.getName());
			assertTrue("Receipt ID not found", file.getName().startsWith("receipt__ID_95b29548-8482-48d7-801d-1901fa8bf208_"));
		}

		assertEquals("Two entries required", 2, fileNameSet.size());
	}

	@Override
	void initialize() throws IOException {
		List<File> files = getAllFilesFromDir(new File(BASE_PATH_SETUP + "/case1"));
		for (File f : files) {
			FileUtils.copyFile(f, new File(BASE_PATH_MH + "/transApp2/outbox", f.getName()));
		}
	}
}