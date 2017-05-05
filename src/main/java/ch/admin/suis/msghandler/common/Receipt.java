/*
 * $Id: Receipt.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.util.ISO8601Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.xml.sax.SAXException;

/**
 * Class to describe the objects holding data from the Sedex adapter's xml.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class Receipt {

	private static RuleSetBase receiptRules = new RuleSetBase() {

		@Override
		public void addRuleInstances(Digester digester) {
			digester.addCallMethod("receipt/messageId", "setMessageId", 0);
			digester.addCallMethod("receipt/recipientId", "setRecipientId", 0);
			digester.addCallMethod("receipt/messageType", "setMessageType", 0,
					new Class[]{Integer.class});
			digester.addCallMethod("receipt/senderId", "setSenderId", 0);
			digester.addCallMethod("receipt/statusCode", "setStatusCode", 0,
					new Class[]{Integer.class});
			digester.addCallMethod("receipt/statusInfo", "setStatusInfo", 0);
			digester.addCallMethod("receipt/eventDate", "setEventDate", 0);
		}

	};

	private MessageType messageType;
	private String messageId;
	private String eventDate;
	private String sentDate;
	private int statusCode;
	private String statusInfo;
	private String recipientId;
	private String senderId;
	private File receiptFile;
	// Added by pirklt
	private String rawData;

	/**
	 * @return Returns the sendertId.
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId The sendertId to set.
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * @return Returns the recipientId.
	 */
	public String getRecipientId() {
		return recipientId;
	}

	/**
	 * @param recipientId The recipientId to set.
	 */
	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}

	/**
	 * @return Returns the eventDate.
	 */
	public String getEventDate() {
		return eventDate;
	}

	/**
	 * Sets the event time for this receipt.
	 *
	 * @param eventDate the event time to set; must be in ISO8601 format
	 * @throws IllegalArgumentException if the parameter value is not in ISO8601 format
	 * @throws NullPointerException     if the parameter is <code>null</code>
	 */
	public void setEventDate(String eventDate) {
		Validate.isTrue(ISO8601Utils.isISO8601Date(eventDate));
		this.eventDate = eventDate;
	}

	/**
	 * @return Returns the messageType.
	 */
	public MessageType getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType The messageType to set.
	 */
	public void setMessageType(Integer messageType) {
		this.messageType = messageType == null ? null
				: new MessageType(messageType);
	}

	/**
	 * Sets the message type.
	 *
	 * @param messageType The message type
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}


	/**
	 * @return Returns the messageId.
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * @param messageId The messageId to set.
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * @return Returns the statusCode.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode The statusCode to set.
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return Returns the statusInfo.
	 */
	public String getStatusInfo() {
		return statusInfo;
	}

	/**
	 * @param statusInfo The statusInfo to set.
	 */
	public void setStatusInfo(String statusInfo) {
		this.statusInfo = statusInfo;
	}


	/**
	 * @return Returns the sentDate.
	 */
	public String getSentDate() {
		return sentDate;
	}

	/**
	 * @param sentDate The sentDate to set.
	 */
	public void setSentDate(String sentDate) {
		Validate.isTrue(ISO8601Utils.isISO8601Date(sentDate));
		this.sentDate = sentDate;
	}

	/**
	 * @return Returns the receiptFile.
	 */
	public File getReceiptFile() {
		return receiptFile;
	}

	/**
	 * @param receiptFile The receiptFile to set.
	 */
	public void setReceiptFile(File receiptFile) {
		this.receiptFile = receiptFile;
	}

	/**
	 * Sets a raw copy of the XML file. Useful in some cases, where some unparsed data is needed.
	 *
	 * @param data String. A Raw XML file.
	 */
	public void setRawData(String data) {
		this.rawData = data;
	}

	/**
	 * Returns the XML Raw data.
	 *
	 * @return String. Raw XML.
	 */
	public String getRawData() {
		return this.rawData;
	}

	/**
	 * Creates a receipt from this reader.
	 *
	 * @param inputStream The flow of data representing a receipt
	 * @return A receipt
	 * @throws IOException  if an error occures while reading XML
	 * @throws SAXException if an error occures while parsing XML
	 */
	public static Receipt createFrom(InputStream inputStream) throws IOException, SAXException {
		final Digester digester = new Digester();
		digester.setNamespaceAware(true);
		digester.addRuleSet(receiptRules);

		final Receipt receipt = new Receipt();
		digester.push(receipt);

		receipt.setRawData(IOUtils.toString(inputStream));
		// As we are using a stream, we are forced to convert it once more
		return (Receipt) digester.parse(IOUtils.toInputStream(receipt.rawData));
	}

	/**
	 * Returns whether this XML file is a legacy version of the receipt or not.
	 *
	 * @return boolean. True if it is legacy, false if it is not.
	 */
	public boolean isLegacy() throws SAXException {
		boolean v1Flag = this.rawData.contains("http://www.ech.ch/xmlns/eCH-0090/1");
		boolean v2Flag = this.rawData.contains("http://www.ech.ch/xmlns/eCH-0090/2");
		if ((v1Flag && v2Flag) || (!v1Flag && !v2Flag)) {
			// Never supposed to happen....
			v1Flag = this.rawData.contains("eCH-0090-1-0.xsd");
			v2Flag = this.rawData.contains("eCH-0090-2-0.xsd");
			if ((v1Flag && v2Flag) || (!v1Flag && !v2Flag)) {
				// Well, nothing I can do here, might as well stop
				throw new SAXException("Judging by the receipt, the receipt version is ambiguous.");
			}
		}
		return v1Flag;
	}
}
