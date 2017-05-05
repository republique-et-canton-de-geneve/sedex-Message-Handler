package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.xml.AbstractType;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * Util classes for JAXB
 *
 * @author pirklt
 */
class XMLGenerator {
	private XMLGenerator() {
		// You're not going to create an instance of this, trust me
	}

	static String formatToString(AbstractType object) throws SAXException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter output = new StringWriter();
			marshaller.marshal(object, output);

			return output.toString();
		} catch (JAXBException e) {
			throw new SAXException("Error while trying to generate XML document", e);
		}
	}
}
