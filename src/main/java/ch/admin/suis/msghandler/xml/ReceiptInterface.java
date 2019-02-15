package ch.admin.suis.msghandler.xml;


import javax.xml.datatype.XMLGregorianCalendar;

public interface ReceiptInterface  {
	XMLGregorianCalendar getEventDate();
	void setEventDate(XMLGregorianCalendar value);

	int getStatusCode();
	void setStatusCode(int value);

	String getStatusInfo();
	void setStatusInfo(String value);

	String getMessageId();
	void setMessageId(String value);

	int getMessageType();
	void setMessageType(int value);

	int getMessageClass();
	void setMessageClass(int value);

	String getSenderId();
	void setSenderId(String value);

	String getRecipientId();
	void setRecipientId(String value);
}
