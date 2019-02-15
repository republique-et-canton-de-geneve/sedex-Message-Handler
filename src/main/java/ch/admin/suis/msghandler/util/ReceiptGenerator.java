package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.common.Message;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.text.ParseException;

/**
 * This interface helps representing the different version of the eCH-0090 XSD Schema.
 * For now, only the first version and the second have been implemented.
 *
 * @author pirklt
 */
public interface ReceiptGenerator {

	/**
	 * This method is used once a receipt is supposed to be generated.
	 *
	 * @param message     The message that is supposed to be used for the receipt
	 * @param recipientID The sender
	 * @return A fully grown XML receipt.
	 * @throws SAXException XML problems...
	 * @throws IOException  IO problems !
	 */
	String generateSuccess(Message message, String recipientID) throws SAXException, IOException, ParseException, DatatypeConfigurationException;

}
