/*
 * $Id$
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

/**
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 24.05.2013
 */
public final class FileFilters {

  public static final String SUFFIX_XML = ".xml";

  public static final String SUFFIX_DPF = ".pdf";

  private static final String PREFIX_ENVL = "envl_";

  /**
   * Filter for XML files. All have to be true: ends with (ignore case) ".xml", can read, is a file
   */
  public static final FileFilter XML_FILTER = new XmlFilter();

  /**
   * Filter for PDF files. All have to be true: ends with (ignore case) ".pdf", can read, is a file
   */
  public static final FileFilter PDF_FILTER = new PdfFilter();

  /**
   * Filter for Envelope files. All have to be true: Pass XmlFilter and starts with "envl_"
   */
  public static final FileFilter ENVELOPE_FILTER = new EnvelopeFilter();

  /**
   * Filter for all files.
   */
  public static final FileFilter ALL_FILES_FILTER = new JustFilesFilter();

  private FileFilters() {
  }

  public static boolean isReadableFile(File pathname) {
    return pathname.isFile() && !pathname.isHidden() && FileUtils.canRead(pathname);
  }

  /**
   * Filter for XML files. All have to be true: ends with (ignore case) ".xml", can read, is a file
   */
  private static final class XmlFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().endsWith(SUFFIX_XML) && isReadableFile(pathname);
    }
  }

  /**
   * Filter for Envelope files. All have to be true: Pass XmlFilter and starts with "envl_"
   */
  private static final class EnvelopeFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return pathname.getName().startsWith(PREFIX_ENVL) && new XmlFilter().accept(pathname);
    }
  }

  private static final class PdfFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().endsWith(SUFFIX_DPF) && isReadableFile(pathname);
    }
  }

  private static final class JustFilesFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      return isReadableFile(pathname);
    }
  }
}