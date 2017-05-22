/*
 * $Id: FileUtils.java 327 2014-01-27 13:07:13Z blaser $
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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.Validate;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The
 * <code>FileUtils</code> class is a utility class to handle the file name transformations.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public final class FileUtils {

	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(FileUtils.class.getName());

	private static final int MAX_FILE_COUNT = 100;

	private static final int BUFFER_SIZE = 2048;

	/**
	 * the counter to
	 */
	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	private static final String NOT_A_DIRECTORY = " is not a directory";

	private FileUtils() {
	}

	/**
	 * Formats the name of the data file for the provided message ID.
	 *
	 * @param messageId The message ID
	 * @return The formed name of the file
	 */
	public static String getDataFilename(String messageId) {
		return "data_" + messageId + ".zip";
	}

	/**
	 * Formats the name of the data file for the provided message ID.
	 *
	 * @param messageId The message ID
	 * @return The formed name of the envelope
	 */
	public static String getEnvelopeFilename(String messageId) {
		return "envl_" + messageId + ".xml";
	}

	/**
	 * Returns a name of the file in the given directory. If the file with the provided name already exists in that
	 * directory, this method returns a new name that is formed by adding the current timestamp value in milliseconds to
	 * the provided name. Otherwise the name remains unchanged. This method returns the absolute filepath.
	 *
	 * @param inputDir where the file should be placed
	 * @param name     current file name
	 * @return the absolute path inclusive filename
	 */
	public static String getFilename(final File inputDir, String name) {
		Validate.isTrue(inputDir.isDirectory(), inputDir.getAbsolutePath()
				+ NOT_A_DIRECTORY);

		File local = new File(inputDir, name);
		if (local.exists()) {
			int count = COUNTER.incrementAndGet();
			count = count % MAX_FILE_COUNT; //

			int insertPos = name.lastIndexOf('.');
			if (insertPos >= 0) {
				// the dot is there, so replace it
				StringBuilder sb = new StringBuilder(name);
				String uniquePart = "-" + System.currentTimeMillis() + "-" + count;
				sb.insert(insertPos, uniquePart);
				return new File(inputDir, sb.toString()).getAbsolutePath();
			} else {
				// just add to the end
				return new File(inputDir, name + "-" + System.currentTimeMillis() + "-" + count)
						.getAbsolutePath();
			}
		} else {
			// the file does not exist yet and can be written without any change to
			// its name
			return local.getAbsolutePath();
		}
	}

	/**
	 * Moves the file to the given directory, renaming it if a file with the same name already exists there.
	 *
	 * @param srcFile The file to move
	 * @param destDir The destination dir
	 */
	public static void moveToDirectory(File srcFile, File destDir) throws IOException {
		Validate.isTrue(!srcFile.isDirectory(), srcFile.getAbsolutePath() + " is a directory");
		Validate.isTrue(destDir.isDirectory(), destDir.getAbsolutePath() + NOT_A_DIRECTORY);

		File destFile = new File(FileUtils.getFilename(destDir, srcFile.getName()));
		moveFile(srcFile, destFile);
	}

	/**
	 * Copies a file into a directory, renaming it if a file with the same name already exists there.
	 *
	 * @param srcFile The file to move
	 * @param destDir The destination dir
	 */
	public static void copyIntoDirectory(File srcFile, File destDir) throws IOException {
		Validate.isTrue(!srcFile.isDirectory(), srcFile.getAbsolutePath() + " is a directory");
		Validate.isTrue(destDir.isDirectory(), destDir.getAbsolutePath() + NOT_A_DIRECTORY);

		File destFile = new File(FileUtils.getFilename(destDir, srcFile.getName()));
		copy(srcFile, destFile);
	}

	/**
	 * Copies the source file to the destination file. Both parameters must be files and not directories. If the
	 * destination exists, it will be overwritten.
	 *
	 * @param source the source file
	 * @param dest   the destination file
	 * @throws IOException              if something does not work
	 * @throws IllegalArgumentException if the source is not a file
	 */
	public static void copy(File source, File dest) throws IOException {
		Validate.isTrue(source.isFile(), source + " is not a file");
		Validate.isTrue(!dest.isDirectory(), dest + " is not a file");

		checkForFreeDiskSpace(source, dest); //Mantis: 0006311

		try (
				InputStream is = new BufferedInputStream(new FileInputStream(source));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(dest))
		) {

			final byte[] data = new byte[BUFFER_SIZE];

			int countBytes;
			while ((countBytes = is.read(data, 0, BUFFER_SIZE)) != -1) {
				os.write(data, 0, countBytes);
			}
		} catch (IOException ex) {
			LOG.error("Unable to copy file.", ex);
			boolean isFileDeleted = false;
			if (dest.exists()) {
				isFileDeleted = dest.delete();
			}
			if (!isFileDeleted) {
				throw new IOException("Unable to delete file " + dest.getAbsolutePath());
			}
			throw ex;
		}
	}

	/**
	 * Returns
	 * <code>true</code>, if the given file can be read by the application. This methods tries to acquire a shared lock
	 * over the specified file. This lock is then immediately released. if an error occurs while acuiring the lock, this
	 * method returns
	 * <code>false</code>.
	 *
	 * @param pathname The path to the file
	 * @return boolean. True if the file can be read, false otherwise.
	 */
	public static boolean canRead(File pathname) {
		if (!pathname.canRead()) {
			return false;
		}

		try (
				FileInputStream fis = new FileInputStream(pathname)
		) {
			FileLock lock = fis.getChannel().tryLock(0, Long.MAX_VALUE, true);

			if (null != lock) {
				// do not hold the lock
				lock.release();

				return true;
			} else {
				LOG.info("cannot lock the file " + pathname.getAbsolutePath()
						+ "; it is probably locked by another application");
				return false;
			}
		} catch (IOException e) {
			LOG.error(
					"an exception occured while trying to acquire lock on the file "
							+ pathname.getAbsolutePath(), e);
			return false;
		}
	}

	/**
	 * Returns a {@link File} object pointing to the absolute path for the supplied name. If the
	 * <code>name</code> parameter already denotes an absolute path, then a {@link File} object is created solely for this
	 * path. Otherwise a {@link File} object is created treating the
	 * <code>parent</code> parameter as the parent directory and the
	 * <code>name</code> parameter as a child path relative to that parent.
	 *
	 * @param parent parent directory
	 * @param name   child path relative to that parent
	 * @return The object pointing to the absolute path
	 */
	public static File createPath(String parent, String name) {
		File nameFile = new File(name);
		if (nameFile.isAbsolute()) {
			return nameFile;
		} else if (StringUtils.isEmpty(parent)) { // parent == null || parent.isEmpty()
			return nameFile.getAbsoluteFile();
		} else {
			File concatFile = new File(new File(parent), name);
			return concatFile.isAbsolute() ? concatFile : concatFile.getAbsoluteFile();
		}
	}

	/**
	 * Moves a file. If destination already exists, it will be delete before the move.<br />Complete logging implemented<p
	 * />
	 * Both parameters have to be files (not directories).
	 *
	 * @param src  Source File
	 * @param dest Destination File
	 */
	public static void moveFile(File src, File dest) throws IOException {

		checkForFreeDiskSpace(src, dest);  //Mantis: 0006311
		boolean isFileDeleted = false;
		try {
			if (dest.exists()) {
				LOG.info("moveFile: " + dest.getAbsoluteFile() + " already exists. Will be overwritten with: " + src.
						getAbsolutePath());
				isFileDeleted = dest.delete();
			}
			if (!isFileDeleted) {
				LOG.error("Unable to delete file: " + dest.getAbsolutePath());
			}
			org.apache.commons.io.FileUtils.moveFile(src, dest);
			LOG.debug("File succesfull moved: " + src.getAbsolutePath() + " to: " + dest.getAbsolutePath());
		} catch (IOException ex) {
			isFileDeleted = false; // Assuming it went wrong here
			LOG.error("Unable to move file from: " + src.getAbsolutePath() + " to: " + dest.getAbsolutePath() + ", reason: "
					+ ex.getMessage(), ex);
			if (dest.exists()) {
				isFileDeleted = dest.delete();
			}
			if (!isFileDeleted) {
				throw new IOException("Unable to delete file " + dest.getAbsolutePath());
			}
			throw ex;
		}
	}

	/**
	 * Checks if the path is an existing file.
	 * <br />Used to validate configuration values.
	 *
	 * @param file The file
	 */
	public static void isFile(String file, String helpText) throws ConfigurationException {
		if (file == null) {
			throwNullPointer(helpText);
		}
		isFile(new File(file), helpText);
	}

	/**
	 * Checks if the path is an existing file. <br />Used to validate configuration values.
	 *
	 * @param file The file
	 */
	public static void isFile(File file, String helpText) throws ConfigurationException {
		if (file == null) {
			throwNullPointer(helpText);
		}
		if (!file.exists() || file.isDirectory()) {
			throw new ConfigurationException("File: " + file.getAbsolutePath()
					+ " either not exist or is not a file. Param name: " + helpText);
		}
	}

	/**
	 * Checks if the path is an existing directory. <br />Used to validate configuration values.
	 *
	 * @param path The path
	 */
	public static void isDirectory(String path, String helpText) throws ConfigurationException {
		if (path == null) {
			throwNullPointer(helpText);
		}

		isDirectory(new File(path), helpText);
	}

	/**
	 * Checks if the path is an existing directory.
	 * <br />Used to validate configuration values.
	 *
	 * @param path The path
	 */
	public static void isDirectory(File path, String helpText) throws ConfigurationException {
		if (path == null) {
			throwNullPointer(helpText);
		}

		if (!path.exists() || !path.isDirectory()) {
			throw new ConfigurationException("Directory: " + path.getAbsolutePath()
					+ " either not exist or is not a directory. Param Name: " + helpText);
		}
	}

	private static void throwNullPointer(String helpText) throws ConfigurationException {
		throw new ConfigurationException("A required configuration parameter is not set. Param Name: " + helpText);
	}

	/**
	 * Reads all files from a directory. Will throw an UnhandledException if the directory doesn't exist or an IO
	 * exception occurred. Use this method instead of File.listFiles(...)
	 *
	 * @param directory  The directory to list the files from
	 * @param filefilter The file filter
	 * @return A list of files (static array)
	 * @Deprecated This should never, ever be used again. It is a very, very old method. See java.nio.file. This method
	 * is not good when you're getting to big data.
	 */
	public static File[] listFiles(File directory, FileFilter filefilter) {
		File[] result = directory.listFiles(filefilter);

		if (result == null) {
			throw new UnhandledException(
					new IOException("This pathname does not denote a directory, or an I/O error occured: "
							+ directory.getAbsolutePath()));
		}

		return result;
	}

	/**
	 * Reads all files from a directory. Will throw an UnhandledException if the directory doesn't exist or an IO
	 * exception occurred. Use this method instead of File.listFiles(...)
	 *
	 * @param directory The directory to list the files from
	 * @param filter    The file filter
	 * @return A stream of files
	 */
	public static DirectoryStream<Path> listFiles(File directory, DirectoryStream.Filter<Path> filter) {
		Path folder = Paths.get(directory.getAbsolutePath());

		try {
			return Files.newDirectoryStream(folder, filter);
		} catch (IOException e) {
			throw new UnhandledException(
					new IOException("This pathname does not denote a directory, or an I/O error occured: "
							+ directory.getAbsolutePath()));
		}
	}

	/**
	 * Returns the number of unallocated bytes in the partition named by this path name. If the parameter points to a
	 * file, the free disk space from the current parent directory will be calculated.
	 *
	 * @param file The file
	 * @return The unallocated bytes
	 */
	public static long getFreeDiskSpace(File file) {
		if (file.isDirectory()) {
			return file.getFreeSpace();
		} else {
			File parent = file.getParentFile();
			if (parent.exists()) {
				return parent.getFreeSpace();
			} else {
				return -1;
			}
		}
	}

	/**
	 * Mantis: 0006311. Checks for free diskspace before copy or move a file!
	 *
	 * @param src  The file to copy
	 * @param dest The destination
	 * @throws IOException Ouch, IO problem here. Probably not enough disk space
	 */
	private static void checkForFreeDiskSpace(File src, File dest) throws IOException {
		long requiredBytes = src.length();
		long freeSpaceBytes = getFreeDiskSpace(dest);

		if (requiredBytes > freeSpaceBytes) {
			String msg = "Not enough free disk space. Required Bytes: " + requiredBytes + ", available Bytes: "
					+ freeSpaceBytes;
			LOG.error("Unable to copy file: src: " + src.getAbsolutePath() + " dest: " + dest.getAbsolutePath()
					+ ". Message: " + msg);
			throw new IOException(msg);
		}
	}
}