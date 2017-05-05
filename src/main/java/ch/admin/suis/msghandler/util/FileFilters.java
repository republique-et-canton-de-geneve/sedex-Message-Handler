/*
 * $Id: FileFilters.java 322 2014-01-21 08:46:53Z blaser $
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
package ch.admin.suis.msghandler.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

/**
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 322 $
 * @since 24.05.2013
 */
public final class FileFilters {

	private FileFilters() {
	}

	public static final String SUFFIX_XML = ".xml";

	public static final String SUFFIX_DPF = ".pdf";

	private static final String PREFIX_ENVL = "envl_";

	/**
	 * Filters for XML files. All have to be true: ends with (ignore case) ".xml", can read, is a file
	 */

	public static final DirectoryStream.Filter<File> XML_FILTER = new DirectoryStream.Filter<File>() {
		@Override
		public boolean accept(File pathname) throws IOException {
			return pathname.getName().toLowerCase().endsWith(SUFFIX_XML) && isReadableFile(pathname);
		}
	};

	public static final DirectoryStream.Filter<Path> XML_FILTER_PATH = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path path) throws IOException {
			return XML_FILTER.accept(path.toFile());
		}
	};

	/**
	 * Filters for PDF files. All have to be true: ends with (ignore case) ".pdf", can read, is a file
	 */
	public static final DirectoryStream.Filter<File> PDF_FILTER = new DirectoryStream.Filter<File>() {
		@Override
		public boolean accept(File pathname) throws IOException {
			return pathname.getName().toLowerCase().endsWith(SUFFIX_DPF) && isReadableFile(pathname);
		}
	};

	public static final DirectoryStream.Filter<Path> PDF_FILTER_PATH = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path path) throws IOException {
			return PDF_FILTER.accept(path.toFile());
		}
	};

	/**
	 * Filters for Envelope files. All have to be true: Pass XmlFilter and starts with "envl_"
	 */
	public static final DirectoryStream.Filter<File> ENVELOPE_FILTER = new DirectoryStream.Filter<File>() {
		@Override
		public boolean accept(File pathname) throws IOException {
			return pathname.getName().startsWith(PREFIX_ENVL) && XML_FILTER.accept(pathname);
		}
	};

	public static final DirectoryStream.Filter<Path> ENVELOPE_FILTER_PATH = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path path) throws IOException {
			return ENVELOPE_FILTER.accept(path.toFile());
		}
	};

	/**
	 * Filters for all files.
	 */
	public static final DirectoryStream.Filter<File> ALL_FILES_FILTER = new DirectoryStream.Filter<File>() {
		@Override
		public boolean accept(File pathname) throws IOException {
			return isReadableFile(pathname);
		}
	};

	public static final DirectoryStream.Filter<Path> ALL_FILES_FILTER_PATH = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path path) throws IOException {
			return ALL_FILES_FILTER.accept(path.toFile());
		}
	};


	/**
	 * Returns whether the file is readable or not
	 *
	 * @param pathname The file in question
	 * @return boolean, true if it is readable or false if it is not.
	 */
	public static boolean isReadableFile(File pathname) {
		return pathname.isFile() && !pathname.isHidden() && FileUtils.canRead(pathname);
	}

	public static boolean isReadableFile(Path path) {
		return isReadableFile(path.toFile());
	}

}