
package ch.admin.suis.msghandler.xml.v1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.admin.suis.msghandler.xml.v1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _V1Envelope_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/1", "V1Envelope");
    private final static QName _V1Receipt_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/1", "V1Receipt");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.admin.suis.msghandler.xml.v1
     * 
     */
    public ObjectFactory() {
        // Not used
    }

    /**
     * Create an instance of {@link V1Envelope }
     * 
     */
    public V1Envelope createEnvelopeType() {
        return new V1Envelope();
    }

    /**
     * Create an instance of {@link V1Receipt }
     * 
     */
    public V1Receipt createReceiptType() {
        return new V1Receipt();
    }

    /**
     * Create an instance of {@link NameValuePairType }
     * 
     */
    public NameValuePairType createNameValuePairType() {
        return new NameValuePairType();
    }

    /**
     * Create an instance of {@link V1Envelope.Loopback }
     * 
     */
    public V1Envelope.Loopback createEnvelopeTypeLoopback() {
        return new V1Envelope.Loopback();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link V1Envelope }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/1", name = "V1Envelope")
    public JAXBElement<V1Envelope> createEnvelope(V1Envelope value) {
        return new JAXBElement<V1Envelope>(_V1Envelope_QNAME, V1Envelope.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link V1Receipt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/1", name = "V1Receipt")
    public JAXBElement<V1Receipt> createReceipt(V1Receipt value) {
        return new JAXBElement<V1Receipt>(_V1Receipt_QNAME, V1Receipt.class, null, value);
    }

}
