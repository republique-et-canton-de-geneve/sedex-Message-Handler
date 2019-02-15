/*
 * $Id: TextProtocolService.java 327 2014-01-27 13:07:13Z blaser $
 *
 * Copyright (C) 2006-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package ch.admin.suis.msghandler.protocol;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.Receipt;
import ch.admin.suis.msghandler.util.ISO8601Utils;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Logs the events through Log4j interface.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class TextProtocolService implements ProtocolService {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String FILENAME_STRING = "filename={0}";
	private static final String MESSAGE_ID_STRING = "messageId={1}";
	private static final String RECIPIENT_ID_STRING = "recipientId={2}";
	private static final String SENT_STRING = "sent={3}";

	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_PREPARING = FILENAME_STRING
			+ LINE_SEPARATOR
			+ MESSAGE_ID_STRING
			+ LINE_SEPARATOR
			+ RECIPIENT_ID_STRING
			+ LINE_SEPARATOR + "preparing={3}" + LINE_SEPARATOR;

	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_FORWARDED = FILENAME_STRING
			+ LINE_SEPARATOR
			+ MESSAGE_ID_STRING
			+ LINE_SEPARATOR
			+ RECIPIENT_ID_STRING
			+ LINE_SEPARATOR + "forwarded={3}" + LINE_SEPARATOR;

	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_SENT =
			FILENAME_STRING
					+ LINE_SEPARATOR +
					MESSAGE_ID_STRING
					+ LINE_SEPARATOR +
					RECIPIENT_ID_STRING
					+ LINE_SEPARATOR +
					SENT_STRING
					+ LINE_SEPARATOR;

	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_DELIVERED = FILENAME_STRING
			+ LINE_SEPARATOR
			+ MESSAGE_ID_STRING
			+ LINE_SEPARATOR
			+ RECIPIENT_ID_STRING
			+ LINE_SEPARATOR +
			SENT_STRING
			+ LINE_SEPARATOR +
			"delivered={4}"
			+ LINE_SEPARATOR;


	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_EXPIRED = FILENAME_STRING
			+ LINE_SEPARATOR
			+ MESSAGE_ID_STRING
			+ LINE_SEPARATOR
			+ RECIPIENT_ID_STRING
			+ LINE_SEPARATOR +
			SENT_STRING
			+ LINE_SEPARATOR +
			"expired={4}"
			+ LINE_SEPARATOR;

	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_ERROR = FILENAME_STRING
			+ LINE_SEPARATOR
			+ MESSAGE_ID_STRING
			+ LINE_SEPARATOR
			+ RECIPIENT_ID_STRING
			+ LINE_SEPARATOR +
			SENT_STRING
			+ LINE_SEPARATOR +
			"error={4}"
			+ LINE_SEPARATOR +
			"description={5}"
			+ LINE_SEPARATOR;


	/**
	 * for receiving
	 */
	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_RECEIVING =
			FILENAME_STRING + LINE_SEPARATOR +
					MESSAGE_ID_STRING + LINE_SEPARATOR +
					"senderId={2}" + LINE_SEPARATOR +
					SENT_STRING + LINE_SEPARATOR +
					"receiving={4}" + LINE_SEPARATOR;


	/**
	 * for receive
	 */
	private static final String GLOBAL_LOG_PROTOCOL_FORMAT_RECEIVED =
			FILENAME_STRING + LINE_SEPARATOR +
					MESSAGE_ID_STRING + LINE_SEPARATOR +
					"senderId={2}" + LINE_SEPARATOR +
					SENT_STRING + LINE_SEPARATOR +
					"received={4}" + LINE_SEPARATOR;

	/**
	 * global logger, we have an instance per a class instance
	 */
	private org.apache.log4j.Logger globalLog = org.apache.log4j.Logger
			.getLogger("GlobalLog");

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logSent(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logPreparing(String filename, Message message) {
		logMessage(filename, GLOBAL_LOG_PROTOCOL_FORMAT_PREPARING, message);
	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logForwarded(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logForwarded(String filename, Message message) {
		logMessage(filename, GLOBAL_LOG_PROTOCOL_FORMAT_FORWARDED, message);
	}

	/**
	 * @param filename the filename
	 * @param format   the log format e.g. GLOBAL_LOG_PROTOCOL_FORMAT_FORWARDED
	 * @param message  the message
	 */
	private void logMessage(String filename, String format, Message message) {
		for (String recipientId : message.getRecipientIds()) {
			final String globalLogText = MessageFormat.format(format,
					filename, message.getMessageId(), recipientId, ISO8601Utils.format(new Date()));
			globalLog.info(globalLogText);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logSent(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logSent(String filename, Receipt receipt) {
		final String globalLogText = MessageFormat.format(
				GLOBAL_LOG_PROTOCOL_FORMAT_SENT,
				filename,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getEventDate()
		);

		globalLog.info(globalLogText);
	}


	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logDelivered(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logDelivered(String filename, Receipt receipt) {
		final String globalLogText = MessageFormat.format(
				GLOBAL_LOG_PROTOCOL_FORMAT_DELIVERED,
				filename,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getEventDate()
		);

		globalLog.info(globalLogText);

	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logExpired(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logExpired(String filename, Receipt receipt) {
		final String globalLogText = MessageFormat.format(
				GLOBAL_LOG_PROTOCOL_FORMAT_EXPIRED,
				filename,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getEventDate()
		);

		globalLog.info(globalLogText);

	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logError(java.lang.String, ch.admin.suis.msghandler.common.Message, ch.admin.suis.msghandler.protocol.ProtocolExceptionWrapper)
	 */
	@Override
	public void logError(String filename, Receipt receipt) {
		final String globalLogText = MessageFormat.format(
				GLOBAL_LOG_PROTOCOL_FORMAT_ERROR,
				filename,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getEventDate(),
				receipt.getStatusInfo()
		);

		globalLog.info(globalLogText);

	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logReceived(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logReceived(String filename, Message message) {
		globalLog.info(MessageFormat.format(GLOBAL_LOG_PROTOCOL_FORMAT_RECEIVED,
				filename,
				message.getMessageId(),
				message.getSenderId(),
				message.getMessageDate(),
				message.getEventDate()
		));
	}

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.msghandler.protocol.ProtocolService#logReceiving(java.lang.String, ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logReceiving(String filename, Message message) {
		globalLog.info(MessageFormat.format(GLOBAL_LOG_PROTOCOL_FORMAT_RECEIVING,
				filename,
				message.getMessageId(),
				message.getSenderId(),
				message.getMessageDate(),
				ISO8601Utils.format(new Date())
		));
	}

}
