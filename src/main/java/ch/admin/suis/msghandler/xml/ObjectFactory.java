
package ch.admin.suis.msghandler.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the ch.admin.suis.msghandler.xml package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

	private static final QName _Envelope_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/2", "envelope");
	private static final QName _Receipt_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/2", "receipt");


	/**
	 * Create an instance of {@link EnvelopeType }
	 */
	public EnvelopeType createEnvelopeType() {
		return new EnvelopeType();
	}

	/**
	 * Create an instance of {@link ReceiptType }
	 */
	public ReceiptType createReceiptType() {
		return new ReceiptType();
	}

	/**
	 * Create an instance of {@link NameValuePairType }
	 */
	public NameValuePairType createNameValuePairType() {
		return new NameValuePairType();
	}

	/**
	 * Create an instance of {@link EnvelopeType.Loopback }
	 */
	public EnvelopeType.Loopback createEnvelopeTypeLoopback() {
		return new EnvelopeType.Loopback();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link EnvelopeType }{@code >}}
	 */
	@XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", name = "envelope")
	public JAXBElement<EnvelopeType> createEnvelope(EnvelopeType value) {
		return new JAXBElement<>(_Envelope_QNAME, EnvelopeType.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ReceiptType }{@code >}}
	 */
	@XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", name = "receipt")
	public JAXBElement<ReceiptType> createReceipt(ReceiptType value) {
		return new JAXBElement<>(_Receipt_QNAME, ReceiptType.class, null, value);
	}

}
