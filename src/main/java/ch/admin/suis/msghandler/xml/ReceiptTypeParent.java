package ch.admin.suis.msghandler.xml;

import ch.admin.suis.msghandler.common.ObjectVersion;
import ch.admin.suis.msghandler.common.Receipt;
import ch.admin.suis.msghandler.xml.v1.V1Receipt;

/**
 * This parents the version of the receipts.
 */
public class ReceiptTypeParent {
	public static Receipt toReceipt(ReceiptInterface receiptInterface){
		Receipt r = new Receipt();
		r.setEventDate(receiptInterface.getEventDate().toString());
		r.setStatusCode(receiptInterface.getStatusCode());
		r.setMessageId(receiptInterface.getMessageId());
		r.setMessageType(receiptInterface.getMessageType());
		r.setSenderId(receiptInterface.getSenderId());
		r.setRecipientId(receiptInterface.getRecipientId());
		r.setStatusInfo(receiptInterface.getStatusInfo());
		r.setRecipientId(receiptInterface.getRecipientId());
		r.setVersion(receiptInterface instanceof V1Receipt ? ObjectVersion.VERSION_1 : ObjectVersion.VERSION_2);
		return r;
	}
}
