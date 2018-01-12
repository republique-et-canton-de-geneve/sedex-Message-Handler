/*
 * $Id: V2ReceiptXmlGenerator.java 292 2013-03-13 13:55:48Z blaser $
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

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.xml.v2.V2Receipt;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Generates a serialized "V1Receipt" object as xml string. See: http://www.ech.ch/xmlns/eCH-0090/1
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 292 $
 * @since 04.01.2013
 */
public final class V2ReceiptXmlGenerator implements ReceiptGenerator {

	/**
	 * Generates a success receipt file for a message. Returns the xml string. Already validated.
	 *
	 * @param message     The message to generate a receipt from
	 * @param recipientId The Recipient ID
	 * @return An XML message
	 * @throws SAXException XML is no good
	 * @throws IOException  The hard drive says no.
	 */
	public String generateSuccess(Message message, String recipientId) throws SAXException, IOException,
			ParseException, DatatypeConfigurationException {


		V2Receipt receipt = new V2Receipt();
		receipt.setVersion("2.0");
		receipt.setEventDate(DateUtils.stringToXMLGregorianCalendar(message.getEventDate()));
		receipt.setMessageId(message.getMessageId());
		receipt.setMessageType(message.getMessageType().getType());
		receipt.setMessageClass(Integer.parseInt(message.getMessageClass()));
		receipt.setSenderId(message.getSenderId());
		receipt.setRecipientId(recipientId);
		// constants...
		receipt.setStatusCode(100);
		receipt.setStatusInfo("Message successfully transmitted");

		File xsdSchema = new File(this.getClass().getResource("/eCH-0090-2-0.xsd").getFile());
		return XMLGenerator.formatToString(receipt, xsdSchema);
	}

}