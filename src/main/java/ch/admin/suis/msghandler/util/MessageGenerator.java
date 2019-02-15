package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.common.Message;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.text.ParseException;

/**
 * This interface represents the different versions of the message generator.
 */
public interface MessageGenerator {
	String generate(Message message) throws SAXException, IOException, ParseException, DatatypeConfigurationException;
}
