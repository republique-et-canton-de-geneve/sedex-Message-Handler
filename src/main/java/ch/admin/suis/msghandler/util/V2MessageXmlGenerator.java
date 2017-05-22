package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.xml.v1.V1Envelope;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
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
			/*
				As of the 18/05/2017, the Swiss Confederation (Federal Statistic Office) asked that sent receipts needed to
				be sent in the v1 format as of a temporary necessity to emit on the SeDex Network.
			 */
			V1Envelope msg = new V1Envelope();
			msg.setVersion("1.0");
			msg.setMessageId(message.getMessageId());
			msg.setMessageType(message.getMessageType().getType());
			msg.setMessageClass(Integer.parseInt(message.getMessageClass()));
			msg.setSenderId(message.getSenderId());
			msg.getRecipientId().addAll(message.getRecipientIds()); // Seriously, what the hell
			msg.setEventDate(DateUtils.stringToXMLGregorianCalendar(message.getEventDate()));
			msg.setMessageDate(DateUtils.stringToXMLGregorianCalendar(message.getMessageDate()));

			File file = new File(this.getClass().getResource("/conf/eCH-0090-1-0.xsd").getFile());
			return XMLGenerator.formatToString(msg, file);
		} catch (ParseException | DatatypeConfigurationException e) {
			throw new SAXException("Failed to generate XML for message " + message.getEnvelopeFile().getAbsolutePath(), e);
		}

	}
}
