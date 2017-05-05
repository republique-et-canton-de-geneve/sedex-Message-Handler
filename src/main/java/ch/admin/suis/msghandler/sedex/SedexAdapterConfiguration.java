/*
 * $Id: SedexAdapterConfiguration.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.sedex;

import ch.admin.suis.msghandler.common.ClientCommons;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

/**
 * The <code>SedexAdapterConfiguration</code> describes what must be configured
 * to access the Sedex adapter.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class SedexAdapterConfiguration {
	private String participantId;
	private String inputDir;
	private String outputDir;
	private String receiptDir;
	private String sentDir;

	/**
	 * Creates a new sedex adapter configuration bean.
	 *
	 * @param participantId the ID of the local participant
	 * @param inputDir      the inbox
	 * @param outputDir     the outbox
	 * @param receiptDir    the receipt directory
	 * @param sentDir       the directory for sent files
	 */
	public SedexAdapterConfiguration(String participantId, String inputDir, String outputDir, String receiptDir,
									 String sentDir) {
		this.participantId = participantId;
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.receiptDir = receiptDir;
		this.sentDir = sentDir;
	}

	/**
	 * @return Returns the inputDir.
	 */
	public String getInputDir() {
		return inputDir;
	}

	/**
	 * @return Returns the outputDir.
	 */
	public String getOutputDir() {
		return outputDir;
	}

	/**
	 * @return Returns the participantId.
	 */
	public String getParticipantId() {
		return participantId;
	}

	/**
	 * @return Returns the receiptDir.
	 */
	public String getReceiptDir() {
		return receiptDir;
	}

	/**
	 * Returns the path to the sent directory of the Sedex adapter.
	 *
	 * @return The path of the sedex "sent" dir.
	 */
	public String getSentDir() {
		return sentDir;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"\n\tparticipantId: {0};" +
						"\n\tinbox folder: {1};" +
						"\n\toutbox folder: {2};" +
						"\n\treceipt folder: {3};" +
						"\n\tsent folder: {4};",
				StringUtils.defaultIfEmpty(participantId, ClientCommons.NOT_SPECIFIED),
				StringUtils.defaultIfEmpty(inputDir, ClientCommons.NOT_SPECIFIED),
				StringUtils.defaultIfEmpty(outputDir, ClientCommons.NOT_SPECIFIED),
				StringUtils.defaultIfEmpty(receiptDir, ClientCommons.NOT_SPECIFIED),
				StringUtils.defaultIfEmpty(sentDir, ClientCommons.NOT_SPECIFIED)
		);
	}


}
