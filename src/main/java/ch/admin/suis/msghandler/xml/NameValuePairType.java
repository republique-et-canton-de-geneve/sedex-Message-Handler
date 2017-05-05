
package ch.admin.suis.msghandler.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Datentyp der die �bergabe von Daten f�r Testzwecke.
 * <p>
 * <p>
 * <p>Java class for nameValuePairType complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType name="nameValuePairType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nameValuePairType", propOrder = {
		"name",
		"value"
})
public class NameValuePairType {

	@XmlElement(required = true)
	protected String name;
	@XmlElement(required = true)
	protected String value;

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the value property.
	 *
	 * @return possible object is
	 * {@link String }
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
