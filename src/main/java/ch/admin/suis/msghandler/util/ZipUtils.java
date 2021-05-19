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
 *
 */

package ch.admin.suis.msghandler.util;

import org.apache.commons.lang.Validate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to help compress and decompress the files for the message
 * handler.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public final class ZipUtils {
  /** logger */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
      .getLogger(ZipUtils.class.getName());

  private static final int BUFFER_SIZE = 2048;

  private ZipUtils(){

  }

  /**
   * Decompress the given file to the specified directory.
   *
   * @param zipFile
   *          the ZIP file to decompress
   * @param toDir
   *          the directory where the files from the archive must be placed; the
   *          file will be replaced if it already exists
   *
   * @return a list of files that were extracted into the destination directory
   * @throws IllegalArgumentException
   *           if the provided file does not exist or the specified destination
   *           is not a directory
   * @throws IOException
   *           if an IO error has occured (probably, a corrupted ZIP file?)
   */
  public static List<File> decompress(File zipFile, File toDir)
      throws IOException {
    Validate.isTrue(zipFile.exists(), "ZIP file does not exist", zipFile
        .getAbsolutePath());
    Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath()
        + " is not a directory");

    final ArrayList<File> files = new ArrayList<File>();

    ZipInputStream zis = null;
    try {
      // open a stream over the zip file
      zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(
          zipFile)));

      // read the entries
      ZipEntry entry;
      while (null != (entry = zis.getNextEntry())) {
        if (entry.isDirectory()) {
          LOG.error(MessageFormat.format("cannot extract the entry {0} from the {1}. because it is a directory",
              entry.getName(), zipFile.getAbsolutePath()));
          continue;
        }

        // extract the file to the provided destination
        BufferedOutputStream dest = null;
        // we have to watch out for a unique name of the file to be extracted:
        // it can happen, that several at the same time incoming messages have a file with the same name
        File extracted = new File(FileUtils.getFilename(toDir, entry.getName()));
        if (!extracted.getParentFile().mkdirs())
        {
          LOG.debug("cannot make all the necessary directories for the file " + extracted.getAbsolutePath() + " or "
              + "the path is already created ");
        }

        try {
          dest = new BufferedOutputStream(new FileOutputStream(extracted), BUFFER_SIZE);

          byte data[] = new byte[BUFFER_SIZE];
          int count;
          while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
            dest.write(data, 0, count);
          }

          files.add(extracted);
        }
        finally {
          if (null != dest) {
            try {
              dest.close();
            }
            catch (IOException e) {
              LOG.error("cannot properly close the file " + extracted.getAbsolutePath(), e);
            }
          }
        }

      }

    }
    finally {
      if (null != zis) {
        try {
          zis.close();
        }
        catch (IOException e) {
          LOG.error("cannot properly close the opened ZIP file " + zipFile.getAbsolutePath(), e);
        }
      }
    }

    return files;
  }


  /**
   * Creates a new unique ZIP file in the destination directory and adds to it
   * the provided collection of files.
   *
   * @param toDir
   * @param files
   * @return
   * @throws IOException if the file cannot be created because of a IO error
   */
  public static File compress(File toDir, Collection<File> files) throws IOException {
    final File zipFile = File.createTempFile("data", ".zip", toDir);

    // was there an exception?
    boolean exceptionThrown = false;

    ZipOutputStream zout = null;
    try {
      zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFFER_SIZE));

      byte data[] = new byte[BUFFER_SIZE];

      for (File file : files) {
        // create the entry
        zout.putNextEntry(new ZipEntry(file.getName()));

        FileInputStream in = new FileInputStream(file);

        FileLock lock = in.getChannel().tryLock(0, Long.MAX_VALUE, true);
        if (null == lock) {
          // the lock cannot be taken, report an Exception
          throw new IOException("cannot apply lock to the file {0}, the file is probably locked by another application");
        }

        try {
          int len;

          // write the file to the entry
          while ((len = in.read(data)) > 0) {
            zout.write(data, 0, len);
          }

        }
        finally {
          lock.release();

          if (null != in) {
            try {
              in.close();
            }
            catch (IOException e) {
              LOG.error("cannot properly close the opened file " + file.getAbsolutePath(), e);
            }
          }
        }
      }
    }
    catch (IOException e) {
      LOG.error("error while creating the ZIP file " + zipFile.getAbsolutePath(), e);
      // mark for the finally block
      exceptionThrown = true;
      // rethrow - the finally block is only for the first exception
      throw e;
    }
    finally {
      if (null != zout) {
        try {
          zout.close();
        }
        catch (IOException e) {
          LOG.error("cannot properly close the opened ZIP file " + zipFile.getAbsolutePath(), e);
        }
      }
      // remove the file in case of an exception
      if(exceptionThrown && !zipFile.delete()) {
        LOG.error("cannot delete the file " + zipFile.getAbsolutePath());
      }
    }

    return zipFile;
  }
}
