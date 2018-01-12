/*
 * $Id: SigningOutbox.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.UnhandledException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SigningOutbox class. This class represent a physical "Signing Outbox" directory. It contains all necessary
 * informations for the sign process. <p /> This is part of the new requirements and configuration from version 3.0.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 03.07.2012
 */
public abstract class SigningOutbox {

	private final File signingOutboxDir;

	/**
	 * null allowed
	 */
	private final File processedDir;

	private final File signingProfile;

	/**
	 * Creates a signingOutbox with a "processed" directory. After successful sign from the PDFs the original/source PDFs
	 * will be moved to the "processed" directory.
	 *
	 * @param signingOutboxDir Directory which may contain 0...n PDFs to sign.
	 * @param signingProfile   Profile (configuration file) for the BatchSinger.
	 * @param processedDir     Null allowed. Successful signed PDFs will be moved to processedDir. If processedDir is null,
	 *                         successful signed PDFs will be deleted.
	 * @throws IllegalArgumentException will be thrown if a directory or file is missing (invalid parameters).
	 */
	public SigningOutbox(File signingOutboxDir, File signingProfile, File processedDir) {

		this.signingOutboxDir = signingOutboxDir;
		this.signingProfile = signingProfile;
		this.processedDir = processedDir;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"\n\tp12 certificate file: {0};" + "\n\tdirectory for the original PDF files: {1};"
						+ "\n\tbatch signer profile: {2};" + "\n\tdirectory for processed files: {3};",
				getP12File().getAbsolutePath(),
				signingOutboxDir.getAbsolutePath(),
				signingProfile.getAbsolutePath(),
				null == processedDir ? ClientCommons.NOT_SPECIFIED : processedDir.getAbsolutePath());
	}

	/**
	 * Returns the (file)name of this SigningOutbox directory.
	 *
	 * @return this.getSigningOutboxDir().getName()
	 */
	public String getName() {
		return getSigningOutboxDir().getName();
	}

	/**
	 * Gets the PKCS12 keystore
	 *
	 * @return The P12 File
	 */
	public abstract File getP12File();

	/**
	 * Gets the password for the P12 (PKCS12) keystore.
	 *
	 * @return The password
	 */
	public abstract String getPassword();

	/**
	 * Will be called before the SigningOutbox will be used. At least one implementation requires a refresh to check
	 * the configuration. If not needed do an empty implementation.
	 */
	public abstract void refresh() throws ConfigurationException;

	/**
	 * Directory where the original PDFs will be moved after successful sign process. This directory can be null. In this
	 * case instead of a move a delete on the original PDFs will be executed.
	 *
	 * @return The processed PDF Directory
	 */
	public File getProcessedDir() {
		return processedDir;
	}

	/**
	 * Source directory with PDFS which have to be signed.
	 *
	 * @return The Directory that signs PDFs.
	 */
	public File getSigningOutboxDir() {
		return signingOutboxDir;
	}

	/**
	 * Returns all PDF files which are in the "signingOutboxDir" directory. These PDFs will be signed later. After a
	 * successful sign process these files should be moved to:
	 * <code>getProcessedDir()</code>. If
	 * <code>getProcessedDir()</code> is null these files should be deleted.
	 *
	 * @return The PDFs that need to be signed
	 */
	public List<File> getAllPDFsToSign() {
		try (DirectoryStream<Path> files = FileUtils.listFiles(signingOutboxDir, FileFilters.PDF_FILTER_PATH)){
			List<File> retVal = new ArrayList<>();
			for (Path path : files) {
				retVal.add(path.toFile());
			}
			files.close();
			return retVal;
		} catch (IOException e){
			throw new UnhandledException(e);
		}
	}

	/**
	 * The signingProfile. This is a BatchSigner configuration.
	 *
	 * @return The certificate property file
	 */
	public File getSigningProfile() {
		return signingProfile;
	}
}