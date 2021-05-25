package ch.admin.suis.msghandler.xml;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.ObjectVersion;
import ch.admin.suis.msghandler.xml.v1.V1Envelope;

/**
 * This parents the version of the envelopes.
 */
public class EnvelopeTypeParent {
	public static Message toMessage(EnvelopeInterface e){
		Message m = new Message();
		m.setMessageId(e.getMessageId());
		m.setMessageType(e.getMessageType());
		m.setMessageClass(Integer.toString(e.getMessageClass()));
		m.setSenderId(e.getSenderId());
		for(String recipient : e.getRecipientId()){
			m.addRecipientId(recipient);
		}
		m.setEventDate(e.getEventDate().toString());
		m.setMessageDate(e.getMessageDate().toString());
		m.setVersion(e instanceof V1Envelope ? ObjectVersion.VERSION_1 : ObjectVersion.VERSION_2);
		return m;
	}
}
