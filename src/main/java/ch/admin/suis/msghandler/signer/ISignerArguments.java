/*
 * $Id: ISignerArguments.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.signer;

/**
 * BatchSigner Arguments. These arguments are required to initialize the BatchSigner. <p />For details see BatchSigner.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 03.07.2012
 */
class ISignerArguments {

	/**
	 * The action by/after sign. These are BatchSigner action.
	 */
	String action = "action";

	/**
	 * The P12 Filename
	 */
	String p12File = "certificatefile";

	/**
	 * Password for the p12File.
	 */
	String p12Password = "certificatepassword";

	/**
	 * Property file for the signing process
	 */
	String signaturePropertyFile = "signatureprofile";

	/**
	 * Type of certificate. Only PKCS12 is supported!
	 */
	String certificationType = "certificatetype";

	/**
	 * Default certifcate type is PKCS12. This is required for version 3.0
	 */
	String defaultCertificationType = "PKCS12";

	/**
	 * Default action type is none. Message Handler implements the action by itself.
	 */
	String defaultAction = "none";

}