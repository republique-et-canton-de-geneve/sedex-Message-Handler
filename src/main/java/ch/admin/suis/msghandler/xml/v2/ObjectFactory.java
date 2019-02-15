
package ch.admin.suis.msghandler.xml.v2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ch.admin.suis.msghandler.xml.v2 package. 
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

    private final static QName _V2Envelope_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/2", "V2Envelope");
    private final static QName _V2Receipt_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0090/2", "V2Receipt");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ch.admin.suis.msghandler.xml.v2
     * 
     */
    public ObjectFactory() {
        // Not used.
    }

    /**
     * Create an instance of {@link V2Envelope }
     * 
     */
    public V2Envelope createEnvelopeType() {
        return new V2Envelope();
    }

    /**
     * Create an instance of {@link V2Receipt }
     * 
     */
    public V2Receipt createReceiptType() {
        return new V2Receipt();
    }

    /**
     * Create an instance of {@link NameValuePairType }
     * 
     */
    public NameValuePairType createNameValuePairType() {
        return new NameValuePairType();
    }

    /**
     * Create an instance of {@link V2Envelope.Loopback }
     * 
     */
    public V2Envelope.Loopback createEnvelopeTypeLoopback() {
        return new V2Envelope.Loopback();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link V2Envelope }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", name = "V2Envelope")
    public JAXBElement<V2Envelope> createEnvelope(V2Envelope value) {
        return new JAXBElement<V2Envelope>(_V2Envelope_QNAME, V2Envelope.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link V2Receipt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", name = "V2Receipt")
    public JAXBElement<V2Receipt> createReceipt(V2Receipt value) {
        return new JAXBElement<V2Receipt>(_V2Receipt_QNAME, V2Receipt.class, null, value);
    }

}
