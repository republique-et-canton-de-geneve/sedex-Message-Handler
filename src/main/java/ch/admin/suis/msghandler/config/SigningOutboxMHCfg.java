/*
 * $Id: SigningOutboxMHCfg.java 286 2013-03-01 10:19:42Z blaser $
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
package ch.admin.suis.msghandler.config;

import java.io.File;

/**
 * The SigningOutbox for MH configuration. That means all parameter for signing are defined in the MessageHandler
 * configuration file. Normally config.xml file.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 286 $
 * @since 27.02.2013
 */
public class SigningOutboxMHCfg extends SigningOutbox {

	private final File p12File;

	private final String password;

	/**
	 * @param p12File          PKCS12 File.
	 * @param password         Password to unlock and sign with certificate in the p12File.
	 * @param signingOutboxDir Directory which may contain 0...n PDFs to sign.
	 * @param signingProfile   Profile (configuration file) for the BatchSinger.
	 * @param processedDir     Null allowed. Successful signed PDFs will be moved to processedDir. If processedDir is null,
	 *                         successful signed PDFs will be deleted.
	 */
	public SigningOutboxMHCfg(File p12File, String password, File signingOutboxDir, File signingProfile, File processedDir) {
		super(signingOutboxDir, signingProfile, processedDir);

		this.p12File = p12File;
		this.password = password;
	}

	/**
	 * Gets the PKCS12 keystore
	 *
	 * @return The P12 file
	 */
	@Override
	public File getP12File() {
		return p12File;
	}

	/**
	 * Gets the password for the P12 (PKCS12) keystore.
	 *
	 * @return The password
	 */
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void refresh() {
		//Nothing to do...
	}
}