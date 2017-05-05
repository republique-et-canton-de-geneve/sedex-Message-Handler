
package ch.admin.suis.msghandler.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for envelopeType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="envelopeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="messageId" type="{http://www.ech.ch/xmlns/eCH-0090/2}messageIdType"/>
 *         &lt;element name="messageType" type="{http://www.ech.ch/xmlns/eCH-0090/2}messageTypeType"/>
 *         &lt;element name="messageClass" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="referenceMessageId" type="{http://www.ech.ch/xmlns/eCH-0090/2}messageIdType" minOccurs="0"/>
 *         &lt;element name="senderId" type="{http://www.ech.ch/xmlns/eCH-0090/2}participantIdType"/>
 *         &lt;element name="recipientId" type="{http://www.ech.ch/xmlns/eCH-0090/2}participantIdType" maxOccurs="unbounded"/>
 *         &lt;element name="eventDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="messageDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="loopback" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="authorise" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="testData" type="{http://www.ech.ch/xmlns/eCH-0090/2}nameValuePairType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="2.0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "envelopeType", namespace = "http://www.ech.ch/xmlns/eCH-0090/2", propOrder = {
		"messageId",
		"messageType",
		"messageClass",
		"referenceMessageId",
		"senderId",
		"recipientId",
		"eventDate",
		"messageDate",
		"loopback",
		"testData"
})
@XmlRootElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", name = "envelope")
public class EnvelopeType implements AbstractType {

	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", required = true)
	protected String messageId;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2")
	protected int messageType;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2")
	protected int messageClass;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2")
	protected String referenceMessageId;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", required = true)
	protected String senderId;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", required = true)
	protected List<String> recipientId;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar eventDate;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar messageDate;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2")
	protected EnvelopeType.Loopback loopback;
	@XmlElement(namespace = "http://www.ech.ch/xmlns/eCH-0090/2")
	protected List<NameValuePairType> testData;
	@XmlAttribute(name = "version", required = true)
	protected String version;

	/**
	 * Gets the value of the messageId property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Sets the value of the messageId property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setMessageId(String value) {
		this.messageId = value;
	}

	/**
	 * Gets the value of the messageType property.
	 */
	public int getMessageType() {
		return messageType;
	}

	/**
	 * Sets the value of the messageType property.
	 */
	public void setMessageType(int value) {
		this.messageType = value;
	}

	/**
	 * Gets the value of the messageClass property.
	 */
	public int getMessageClass() {
		return messageClass;
	}

	/**
	 * Sets the value of the messageClass property.
	 */
	public void setMessageClass(int value) {
		this.messageClass = value;
	}

	/**
	 * Gets the value of the referenceMessageId property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getReferenceMessageId() {
		return referenceMessageId;
	}

	/**
	 * Sets the value of the referenceMessageId property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setReferenceMessageId(String value) {
		this.referenceMessageId = value;
	}

	/**
	 * Gets the value of the senderId property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * Sets the value of the senderId property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setSenderId(String value) {
		this.senderId = value;
	}

	/**
	 * Gets the value of the recipientId property.
	 * <p>
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the recipientId property.
	 * <p>
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getRecipientId().add(newItem);
	 * </pre>
	 * <p>
	 * <p>
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link String }
	 */
	public List<String> getRecipientId() {
		if (recipientId == null) {
			recipientId = new ArrayList<>();
		}
		return this.recipientId;
	}

	/**
	 * Gets the value of the eventDate property.
	 *
	 * @return possible object is
	 * {@link XMLGregorianCalendar }
	 */
	public XMLGregorianCalendar getEventDate() {
		return eventDate;
	}

	/**
	 * Sets the value of the eventDate property.
	 *
	 * @param value allowed object is
	 *              {@link XMLGregorianCalendar }
	 */
	public void setEventDate(XMLGregorianCalendar value) {
		this.eventDate = value;
	}

	/**
	 * Gets the value of the messageDate property.
	 *
	 * @return possible object is
	 * {@link XMLGregorianCalendar }
	 */
	public XMLGregorianCalendar getMessageDate() {
		return messageDate;
	}

	/**
	 * Sets the value of the messageDate property.
	 *
	 * @param value allowed object is
	 *              {@link XMLGregorianCalendar }
	 */
	public void setMessageDate(XMLGregorianCalendar value) {
		this.messageDate = value;
	}

	/**
	 * Gets the value of the loopback property.
	 *
	 * @return possible object is
	 * {@link EnvelopeType.Loopback }
	 */
	public EnvelopeType.Loopback getLoopback() {
		return loopback;
	}

	/**
	 * Sets the value of the loopback property.
	 *
	 * @param value allowed object is
	 *              {@link EnvelopeType.Loopback }
	 */
	public void setLoopback(EnvelopeType.Loopback value) {
		this.loopback = value;
	}

	/**
	 * Gets the value of the testData property.
	 * <p>
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the testData property.
	 * <p>
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getTestData().add(newItem);
	 * </pre>
	 * <p>
	 * <p>
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link NameValuePairType }
	 */
	public List<NameValuePairType> getTestData() {
		if (testData == null) {
			testData = new ArrayList<>();
		}
		return this.testData;
	}

	/**
	 * Gets the value of the version property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setVersion(String value) {
		this.version = value;
	}


	/**
	 * <p>Java class for anonymous complex type.
	 * <p>
	 * <p>The following schema fragment specifies the expected content contained within this class.
	 * <p>
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;attribute name="authorise" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Loopback {

		@XmlAttribute(name = "authorise", required = true)
		protected boolean authorise;

		/**
		 * Gets the value of the authorise property.
		 */
		public boolean isAuthorise() {
			return authorise;
		}

		/**
		 * Sets the value of the authorise property.
		 */
		public void setAuthorise(boolean value) {
			this.authorise = value;
		}

	}

}
