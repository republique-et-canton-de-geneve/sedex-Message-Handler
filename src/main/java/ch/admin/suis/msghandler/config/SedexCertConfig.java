/*
 * $Id: SedexCertConfig.java 286 2013-03-01 10:19:42Z blaser $
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
import java.util.Date;

/**
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 286 $
 * @since 27.02.2013
 */
public class SedexCertConfig {

	private final File p12File;

	private final String password;

	private final Date expireDate;

	public SedexCertConfig(File p12File, String password, Date expireDate) {
		this.p12File = p12File;
		this.password = password;
		this.expireDate = expireDate;
	}

	public File getP12File() {
		return p12File;
	}

	public String getPassword() {
		return password;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("p12 file: " + p12File.getAbsolutePath());
		if (expireDate != null) {
			sb.append(", expireDate: ").append(expireDate);
		}
		return sb.toString();
	}
}