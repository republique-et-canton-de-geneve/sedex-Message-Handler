package ch.admin.suis.msghandler.xml;


import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public interface EnvelopeInterface {
	String getMessageId();
	void setMessageId(String value);

	int getMessageType();
	void setMessageType(int value);

	int getMessageClass();
	void setMessageClass(int value);

	String getReferenceMessageId();
	void setReferenceMessageId(String value);

	String getSenderId();
	void setSenderId(String value);

	List<String> getRecipientId();

	XMLGregorianCalendar getEventDate();
	void setEventDate(XMLGregorianCalendar value);

	XMLGregorianCalendar getMessageDate();
	void setMessageDate(XMLGregorianCalendar value);



}