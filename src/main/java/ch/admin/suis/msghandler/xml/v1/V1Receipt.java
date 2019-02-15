
package ch.admin.suis.msghandler.xml.v1;

import ch.admin.suis.msghandler.xml.ReceiptInterface;
import ch.admin.suis.msghandler.xml.ReceiptTypeParent;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for V1Receipt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="V1Receipt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="statusCode" type="{http://www.ech.ch/xmlns/eCH-0090/1}statusCodeType"/>
 *         &lt;element name="statusInfo" type="{http://www.ech.ch/xmlns/eCH-0090/1}string255Type"/>
 *         &lt;element name="messageId" type="{http://www.ech.ch/xmlns/eCH-0090/1}messageIdType"/>
 *         &lt;element name="messageType" type="{http://www.ech.ch/xmlns/eCH-0090/1}messageTypeType"/>
 *         &lt;element name="messageClass" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="senderId" type="{http://www.ech.ch/xmlns/eCH-0090/1}participantIdType"/>
 *         &lt;element name="recipientId" type="{http://www.ech.ch/xmlns/eCH-0090/1}participantIdType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="1.0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "V1Receipt", propOrder = {
    "eventDate",
    "statusCode",
    "statusInfo",
    "messageId",
    "messageType",
    "messageClass",
    "senderId",
    "recipientId"
})
@XmlRootElement(name = "receipt")
public class V1Receipt extends ReceiptTypeParent implements ReceiptInterface {

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar eventDate;
    protected int statusCode;
    @XmlElement(required = true)
    protected String statusInfo;
    @XmlElement(required = true)
    protected String messageId;
    protected int messageType;
    protected int messageClass;
    @XmlElement(required = true)
    protected String senderId;
    @XmlElement(required = true)
    protected String recipientId;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Gets the value of the eventDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEventDate() {
        return eventDate;
    }

    /**
     * Sets the value of the eventDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEventDate(XMLGregorianCalendar value) {
        this.eventDate = value;
    }

    /**
     * Gets the value of the statusCode property.
     * 
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the value of the statusCode property.
     * 
     */
    public void setStatusCode(int value) {
        this.statusCode = value;
    }

    /**
     * Gets the value of the statusInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusInfo() {
        return statusInfo;
    }

    /**
     * Sets the value of the statusInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusInfo(String value) {
        this.statusInfo = value;
    }

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the messageType property.
     * 
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Sets the value of the messageType property.
     * 
     */
    public void setMessageType(int value) {
        this.messageType = value;
    }

    /**
     * Gets the value of the messageClass property.
     * 
     */
    public int getMessageClass() {
        return messageClass;
    }

    /**
     * Sets the value of the messageClass property.
     * 
     */
    public void setMessageClass(int value) {
        this.messageClass = value;
    }

    /**
     * Gets the value of the senderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets the value of the senderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderId(String value) {
        this.senderId = value;
    }

    /**
     * Gets the value of the recipientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipientId() {
        return recipientId;
    }

    /**
     * Sets the value of the recipientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipientId(String value) {
        this.recipientId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
