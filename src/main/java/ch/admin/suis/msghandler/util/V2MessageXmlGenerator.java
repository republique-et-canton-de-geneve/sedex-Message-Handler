package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.xml.EnvelopeType;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.text.ParseException;

/**
 * Implements the new envelope schema.
 */
public class V2MessageXmlGenerator implements MessageGenerator {

	/**
	 * Returns a V2 envelope.
	 *
	 * @param message The message used to fill the infos with
	 * @return The envelope.
	 * @throws SAXException When an XML file is not right
	 * @throws IOException  When there's an IO problem
	 */
	public String generate(Message message) throws SAXException, IOException {
		try {
			EnvelopeType msg = new EnvelopeType();
			msg.setVersion("2.0");
			msg.setMessageId(message.getMessageId());
			msg.setMessageType(message.getMessageType().getType());
			msg.setMessageClass(Integer.parseInt(message.getMessageClass()));
			msg.setSenderId(message.getSenderId());
			msg.getRecipientId().addAll(message.getRecipientIds()); // Seriously, what the hell
			msg.setEventDate(DateUtils.stringToXMLGregorianCalendar(message.getEventDate()));
			msg.setMessageDate(DateUtils.stringToXMLGregorianCalendar(message.getMessageDate()));

			String xmlString = XMLGenerator.formatToString(msg);
			XMLValidator.validateEch0090_2(xmlString);
			return xmlString;
		} catch (ParseException | DatatypeConfigurationException e) {
			throw new SAXException("Failed to generate XML for message " + message.getEnvelopeFile().getAbsolutePath(), e);
		}

	}
}
