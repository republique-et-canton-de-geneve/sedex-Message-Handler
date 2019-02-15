package ch.admin.suis.msghandler.util;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.StringWriter;

/**
 * Util classes for JAXB
 *
 * @author pirklt
 */
public class XMLGenerator {
	private XMLGenerator() {
		// You're not going to create an instance of this, trust me
	}

	public static String formatToString(Object object, File pathToXSD) throws SAXException {
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(pathToXSD);

			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter output = new StringWriter();
			marshaller.marshal(object, output);

			return output.toString();
		} catch (JAXBException e) {
			throw new SAXException("Error while trying to generate XML document", e);
		}
	}
}
