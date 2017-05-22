/*
 * $Id: MessageXmlGenerator.java 296 2013-03-13 15:26:59Z blaser $
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
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Generates a serialized "Message" object as xml string. See: http://www.ech.ch/xmlns/eCH-0090/1
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 296 $
 * @since 26.02.2013
 * @deprecated
 */
@SuppressWarnings("ALL")
@Deprecated
public final class V1MessageXmlGenerator {

  /* Example Message
   <?xml version="1.0" encoding="UTF-8"?><envelope xmlns="http://www.ech.ch/xmlns/eCH-0090/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ech.ch/xmlns/eCH-0090/1 eCH-0090-1-0.xsd" version="1.0">
   <messageId>95b29548-8482-48d7-801d-1901fa8bf204</messageId>
   <messageType>10003</messageType>
   <messageClass>0</messageClass>
   <senderId>7-4-4</senderId>
   <recipientId>7-4-2</recipientId>
   <recipientId>7-4-5</recipientId>
   <eventDate>2013-01-08T08:49:00.38+01:00</eventDate><messageDate>2013-01-08T08:49:00.38+01:00</messageDate></envelope>
   */

	/**
	 * the template to format the message in XML
	 */
	private static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<envelope xmlns=\"http://www.ech.ch/xmlns/eCH-0090/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
//          + "xsi:schemaLocation=\"http://www.ech.ch/xmlns/eCH-0090/1 eCH-0090-1-0.xsd\" version=\"1.0\">\n"
			+ "version=\"1.0\">\n"
			+ " <messageId>{0}</messageId>\n"
			+ " <messageType>{1}</messageType>\n"
			+ " <messageClass>{2}</messageClass>\n"
			+ " <senderId>{3}</senderId>\n"
			+ "{4}"
			+ " <eventDate>{5}</eventDate>\n"
			+ " <messageDate>{6}</messageDate>\n"
			+ "</envelope>";


	/**
	 * Generates an xml string from the message object. Already validated.
	 *
	 * @param message
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public String generate(Message message) throws SAXException, IOException {
		//Hack because there could be multiple recipientIds. So it's not possible to do that with a MessageFormat and a TEMPLATE.
		StringBuilder sb = new StringBuilder();
		for (String recipient : message.getRecipientIds()) {
			sb.append(" <recipientId>").append(recipient).append("</recipientId>\n");
		}

		final String xmlString = MessageFormat.format(TEMPLATE,
				message.getMessageId(), message.getMessageType(), message.getMessageClass(),
				message.getSenderId(), sb.toString(), message.getEventDate(), message.getMessageDate());

		XMLValidator.validateEch0090_1(xmlString);
		return xmlString;
	}
}