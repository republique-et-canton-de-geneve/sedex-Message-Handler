/*
 * $Id: ReceiverSessionImpl.java 340 2015-08-16 14:51:19Z sasha $
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

package ch.admin.suis.msghandler.receiver;

import ch.admin.suis.msghandler.common.*;
import ch.admin.suis.msghandler.config.ClientConfiguration;
import ch.admin.suis.msghandler.config.Inbox;
import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The implementation of the <code>ReceiverSession</code> interface used by
 * the message handler. The instances of this class are neither reusable nor
 * thread-safe.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public class ReceiverSessionImpl implements ReceiverSession, ClientCommons {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ReceiverSessionImpl.class.getName());

	private MessageHandlerContext context;

	/**
	 * Constructor
	 *
	 * @param context the MessageHandlerContext
	 */
	public ReceiverSessionImpl(MessageHandlerContext context) {
		this.context = context;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Semaphore getDefenseLock() {
		return context.getDefenseLock();
	}

	/**
	 * Checks for the new messages in the inbox folder of the Sedex Adapter and extract the files to a temporary
	 * directory
	 * returning the message objects wrapping those files.
	 *
	 * @see ch.admin.suis.msghandler.receiver.ReceiverSession#getNewMessages()
	 */
	@Override
	public Collection<IncomingMessage> getNewMessages() {
		// the inbox folder of the sedex adapter
		final File sedexInputDir = new File(context.getClientConfiguration()
				.getSedexAdapterConfiguration().getInputDir());

		// create a message for each envelope
		ArrayList<IncomingMessage> messages = new ArrayList<>();

		for (Message message : new MessageCollection(sedexInputDir).get()) {

			IncomingMessage incomingMessage = createIncomingMessage(message);
			// determine the inbox for this message
			if (incomingMessage.getInboxes().isEmpty()) {
				// no inbox found
				LOG.error(MessageFormat
						.format(
								"an inbox cannot be assigned to the file {0} with message type {1}; the file will be moved to the"
										+ " 'unknown' directory",
								message.getDataFile(), message.getMessageType()));
			} else {
				// extract the files to the temporary directory and store the references
				// in the message
				try {
					extractTo(incomingMessage);
					// if the message is corrupted, then there will be no files added to
				} catch (IOException e) {
					LOG.error("cannot receive/extract the data file for the envelope "
							+ message.getEnvelopeFile().getAbsolutePath() + "; file is skipped", e);
				}
			}

			// we add this message
			messages.add(incomingMessage);
		}

		return messages;
	}

	/**
	 * Copies this message to the corresponding inbox or to the "corrupted" directory.
	 *
	 * @see ch.admin.suis.msghandler.receiver.ReceiverSession#receive(IncomingMessage)
	 */
	@Override
	public void receive(IncomingMessage incomingMessage) throws IOException {

		final ClientConfiguration clientConfig = context.getClientConfiguration();
		final File unknownDir = new File(new File(clientConfig.getWorkingDir()), UNKNOWN_DIR);

		final Message message = incomingMessage.getMessage();
		final List<Inbox> inboxes = incomingMessage.getInboxes();

		if (inboxes.isEmpty()) {
			// move to the unknown???
			FileUtils.copy(message.getEnvelopeFile(), new File(unknownDir, message.getEnvelopeFile().getName()));
			FileUtils.copy(message.getDataFile(), new File(unknownDir, message.getDataFile().getName()));

			// if everything is ok
			LOG.info(MessageFormat.format("file {0} received, but put into the unknown directory {1}",
					message.getDataFile().getName(), unknownDir));

			return;
		}

		for (Inbox inbox : inboxes) {
			inbox.receive(context, message);
		}

		for (File incomingFile : message.getFiles()) {
			if (!incomingFile.delete()) {
				LOG.warn("Unable to delete file: " + incomingFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Writes the protocol and removes the incoming data and envelope files
	 * from the inbox of the Sedex adapter.
	 *
	 * @see ch.admin.suis.msghandler.receiver.ReceiverSession
	 */
	@Override
	public void logSuccess(IncomingMessage incomingMessage) {
		final Message message = incomingMessage.getMessage();

		for (File file : message.getFiles()) {
			context.getProtocolService().logReceived(file.getName(), message);
		}

		// cleanup the temporary files
		cleanup(incomingMessage);

		// envelope
		if (message.getEnvelopeFile().delete()) {
			LOG
					.debug(MessageFormat
							.format(
									"envelope file {0} successfully removed from the input directory of the sedex adapter",
									message.getEnvelopeFile()));
		} else {
			LOG
					.debug(MessageFormat
							.format(
									"envelope file {0} cannot be removed from the input directory of the sedex adapter",
									message.getEnvelopeFile()));
		}

		// data file
		if (message.getDataFile().delete()) {
			LOG
					.info(MessageFormat
							.format(
									"data file {0} successfully removed from the input directory of the sedex adapter",
									message.getDataFile()));
		} else {
			LOG
					.error(MessageFormat
							.format(
									"data file {0} cannot be removed from the input directory of the sedex adapter",
									message.getDataFile()));
		}
	}

	/**
	 * Removes the temporary files (if any).
	 *
	 * @see ch.admin.suis.msghandler.receiver.ReceiverSession
	 */
	@Override
	public void logError(IncomingMessage incomingMessage, Exception e) {
		final Message message = incomingMessage.getMessage();

		LOG.error("cannot not receive the message " + message.getMessageId(), e);

		// cleanup the temporary files
		cleanup(incomingMessage);
	}

	/**
	 * Does nothing.
	 *
	 * @see ch.admin.suis.msghandler.receiver.ReceiverSession#cleanup()
	 */
	@Override
	public void cleanup() {
		//You may wonder why I'm empty, but trust me, it's for the greater good
	}

	/**
	 * Removes the temporary files for this incoming message.
	 */
	private void cleanup(IncomingMessage incomingMessage) {
		final Message message = incomingMessage.getMessage();

		for (File file : message.getFiles()) {
			// remove this file
			if (!StringUtils.equals(message.getDataFile().getAbsolutePath(), file.getAbsolutePath())) {
				deleteFile(file);
			}
		}

	}

	/**
	 * Deletes a file according to the inbox mode.
	 *
	 * @param file The file to delete.
	 */
	private void deleteFile(File file) {
		if (file.exists()) { // this is not the data file itself (the inbox is not transparent)
			if (file.delete()) {
				LOG.info(MessageFormat
						.format(
								"file {0} successfully removed from the temporary directory",
								file));
			} else {
				LOG.error(MessageFormat
						.format(
								"file {0} cannot be removed from the temporary directory",
								file));
			}
		}
	}

	private void extractTo(IncomingMessage incomingMessage) throws IOException {
		final Message message = incomingMessage.getMessage();
		final List<Inbox> inboxes = incomingMessage.getInboxes();

		for (Inbox inbox : inboxes) {
			inbox.extract(context, message);
		}
	}

	/**
	 * Creates the incoming message.
	 *
	 * @param message The message to transform.
	 * @return a new incoming message or <code>null</code>, if the inbox cannot be found
	 */
	private IncomingMessage createIncomingMessage(Message message) {
		final ClientConfiguration clientConfig = context.getClientConfiguration();
		// continue and move to the corresponding inbox
		final ReceiverConfiguration configuration = clientConfig.getReceiverConfiguration();

		IncomingMessage incomingMessage = new IncomingMessage(message);

		// use the configuration to determine the inbox
		for (Inbox inbox : configuration.getInboxes()) {
			for (String recipientId : message.getRecipientIds()) {
				// both inbox.sedexId and messageType should match those of the message
				if (StringUtils.equals(inbox.getSedexId(), recipientId) && inbox.getMessageTypes().contains(
						message.getMessageType())) {
					// the message type is resolved
					incomingMessage.addInbox(inbox);
				}
			}
		}

		if (incomingMessage.getInboxes().isEmpty()) {
			LOG.warn("Unable to determine an inbox for: sedexId: " + message.getRecipientsAsString() + ", msgType: "
					+ message.getMessageType().toString());
		}

		return incomingMessage;
	}

}
