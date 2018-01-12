/*
 * $Id: StatusCheckerSessionImpl.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.checker;


import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.Receipt;
import ch.admin.suis.msghandler.common.ReceiptsFolder;
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.log.LogServiceException;
import ch.admin.suis.msghandler.log.LogStatus;
import ch.admin.suis.msghandler.log.ProtocolWriter;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ISO8601Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

import static ch.admin.suis.msghandler.common.ClientCommons.*;

/**
 * The implementation of the <code>StatusCheckerSession</code> interface for the
 * Sedex adapter.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class StatusCheckerSessionImpl implements StatusCheckerSession {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(StatusCheckerSessionImpl.class.getName());

	private MessageHandlerContext context;

	private static final String MSG_NOT_A_DIRECTORY = " is not a directory";

	/**
	 * Creates a new instance of this class.
	 *
	 * @param context MessageHandlerContext
	 */
	public StatusCheckerSessionImpl(MessageHandlerContext context) {
		this.context = context;
	}

	/**
	 * Returns the lock, so that the checker can reinforce its non-interruptability
	 * while performing critical tasks.
	 *
	 * @see ch.admin.suis.msghandler.checker.StatusCheckerSession#getDefenseLock()
	 */
	@Override
	public Semaphore getDefenseLock() {
		return context.getDefenseLock();
	}

	/**
	 * Looks into the internal DB and selects the IDs of the messages that
	 * have the status SENT or FORWARDED. Then this method checks the receipts directory
	 * of the Sedex adapter to see, for which message there is already a receipt.
	 * The list of the found receipt is then returned. If there are no receipts, this
	 * method returns an empty collection.
	 *
	 * @see ch.admin.suis.msghandler.checker.StatusCheckerSession#getMessagesIds()
	 */
	@Override
	public Collection<Receipt> getMessagesIds() throws LogServiceException {
		ArrayList<Receipt> receipts = new ArrayList<>();

		// the internal DB
		final LogService logService = context.getLogService();

		// the Sedex adapter's receipt directory
		File receiptsDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getReceiptDir());

		// get the messages that have either FORWARDED or SENT as their status
		TreeSet<String> sentIds = new TreeSet<>(logService.getSentMessages());

		// loop over the files in the receipts directory
		// check for the files over there
		DirectoryStream<Path> files = FileUtils.listFiles(receiptsDir, FileFilters.XML_FILTER_PATH);

		if (files == null) {
			LOG.error("an I/O error occured while reading the receipts from the Sedex adapter; " +
					"check the message handler configuration to see whether the specified 'receipts' directory " +
					"for the Sedex Adapter actually exists");
			return Collections.emptyList();
		}

		//
		ArrayList<String> toBeRemoved = new ArrayList<>();
		// for each receipt found
		for (Path path : files) {
			try (InputStream reader = Files.newInputStream(path)) {
				Receipt receipt = Receipt.createFrom(reader);
				if (!sentIds.contains(receipt.getMessageId())) {
					continue;
				}
				// get the sent date for this receipt (it is not unfortunately in the receipt XML)
				receipt.setSentDate(ISO8601Utils.format(logService.getSentDate(receipt.getMessageId())));
				receipt.setReceiptFile(path.toFile());
				receipts.add(receipt);// add it now
				LOG.info(MessageFormat.format("message ID {0}: receipt found", receipt.getMessageId()));
				// set to remove the id from the tree
				toBeRemoved.add(receipt.getMessageId());
			} catch (FileNotFoundException e) {
				LOG.error("cannot find the file " + path.toString() + "; is it already removed?", e);
			} catch (IOException e) {
				LOG.error("cannot read the file " + path.toString(), e);
			} catch (JAXBException e) {
				LOG.error("cannot parse the file " + path.toString(), e);
			} catch (LogServiceException e){
				closeStream(files);
				throw e; // In order to keep the current exception flow
			}

		}
		closeStream(files);

		// remove from the list
		sentIds.removeAll(toBeRemoved);

		// now, lets look at what has remained to find out, whether the Sedex adapter has just sent the files
		// but not received the receipt (look only at forwarded messages that are not "transparent")
		final File outputDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getOutputDir());

		for (final String messageId : logService.getMessages(LogStatus.FORWARDED)) {

			// Skips execution if not all of the conditions below match
			if (sentIds.contains(messageId) && !logService.isTransparent(messageId) &&
					!new File(outputDir, FileUtils.getDataFilename(messageId)).exists()) {

				// the envelope that we have created
				final Message message = getSentMessage(messageId);
				if (message == null) {
					// the file is send by the adapter but there is no receipt yet
					LOG.warn(MessageFormat.format("message ID {0}: message sent by the Sedex adapter, but there is no envelope in the Sedex sent directory",
							messageId));
					continue;
				}
				// For each recipient, we generate a receipt
				for (String recipientId : message.getRecipientIds()) {
					receipts.add(generateReceipt(message, recipientId, messageId));
				}
				LOG.info("message has been sent by the Sedex adapter: " + messageId);

				// remove the id from the tree
				sentIds.remove(messageId);

			}


		}
		/* TODO sort out the receipts so that we can reliably process the situation where
           there is more than one receipt pro message*/
		return receipts;
	}

	private void closeStream(DirectoryStream stream){
		try{
			stream.close();
		} catch (IOException e){
			LOG.error("Unable to close directory stream. " + e);
		}
	}


	/**
	 * Generates a simple receipt
	 *
	 * @param message     The message to generate from
	 * @param recipientId The Recipient ID
	 * @param messageId   The Message ID
	 * @return A Receipt.
	 */
	private Receipt generateReceipt(Message message, String recipientId, String messageId) {
		Receipt r = new Receipt();
		r.setEventDate(ISO8601Utils.format(new Date()));
		r.setMessageId(messageId);
		r.setStatusCode(0);
		r.setStatusInfo("the message is sent by the adapter; no receipt yet");
		r.setSentDate(message.getEventDate());
		r.setRecipientId(recipientId);
		return r;
	}

	/**
	 * Returns a message object for the given message ID by reading
	 * the envelope file in the specified directory
	 *
	 * @param messageId the message ID
	 * @return the message object or <code>null</code> if nothing has been found
	 * or the message cannot be read
	 */
	private Message getSentMessage(String messageId) {
		File sentDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getSentDir());
		final File envelope = new File(sentDir, FileUtils.getEnvelopeFilename(messageId));

    /*
     * "Bugfix". The problem is: Sedex does not know MessageHandler. MessageHandler checks the sedex outbox and sedex
     * sent directory for the data and envelope file. But if sedex moves first the data file then the data file is in
     * the sedex sent directory. If now MH checks the sent directory then the envelope file is missing (because sedex is
     * too slow and didn't yet moved the env. file). This bugfix checks this case and will not log it as error. It will
     * log this as error when MHs log-level is debug or below..
     */
		if (!envelope.exists()) {
			LOG.warn("Sedex Sent directory does not contain file: " + envelope.getAbsolutePath()
					+ ". Maybe Sedex moved the data file before the env file.");
			if (LOG.isDebugEnabled()) {
				String msg = "cannot read the envelope file " + envelope.getAbsolutePath();
				LOG.error(msg, new FileNotFoundException(msg));
			}
			return null;
		}

		try (InputStream reader = new FileInputStream(envelope)) {
			return Message.createFrom(reader);
		} catch (IOException e) {
			LOG.error("cannot read the envelope file " + envelope.getAbsolutePath(), e);
			return null;
		} catch (JAXBException e) {
			LOG.error("cannot parse the envelope file " + envelope.getAbsolutePath(), e);
			return null;
		}

	}


	/**
	 * Updates the status for the message corresponding to this receipt.
	 *
	 * @see ch.admin.suis.msghandler.checker.StatusCheckerSession(java.lang.String)
	 */
	@Override
	public void updateStatus(Receipt receipt) throws LogServiceException {
		// the internal DB
		final LogService logService = context.getLogService();

		final ProtocolService protocolService = context.getProtocolService();

		File sentDir = new File(new File(context.getClientConfiguration().getWorkingDir()), SENT_DIR);

		switch (receipt.getStatusCode()) {
			case 0:
				// that is what we have set; sent

				// update the status in the internal DB
				logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.SENT);

				// log the event. for each file in the message. NOTE : the getFiles can throw a LogServiceException
				for (String filename : logService.getFiles(receipt.getMessageId())) {
					protocolService.logSent(filename, receipt);
				}

				// and write the protocol
				break;
			case 100:
				handle100Receipt(receipt, logService, protocolService, sentDir);
				break;
			case 500:
				handle500Receipt(receipt, logService, protocolService, sentDir);
				break;
			case 320:
			case 204:
				handle204Receipt(receipt, logService, protocolService, sentDir);
				break;
			case 601:
				LOG.info(MessageFormat.format("message ID {0}: the Sedex adapter has successfully transferred the " +
						"message to the intermediary server, status '{1}'", receipt.getMessageId(), receipt.getStatusInfo()
				));
				// the message was transfered
				// TODO implement the functionality when the message is transferred
				break;
			default:
				LOG.info("Received code " + receipt.getStatusCode() + ", which is unsupported.");
		}

	}

	/**
	 * Do its stuff about any receipt that has a 100 code.
	 *
	 * @param receipt         The receipt
	 * @param logService      The log service
	 * @param protocolService The protocol service
	 * @param sentDir         The directory for the sent stuff
	 * @throws LogServiceException Woops
	 */
	private void handle100Receipt(Receipt receipt, LogService logService, ProtocolService protocolService,
								  File sentDir) throws LogServiceException {

		// everything is ok; mark as delivered
		LOG.info(MessageFormat.format("message ID {0}: the message has been delivered by the Sedex adapter, status {1}",
				receipt.getMessageId(), receipt.getStatusInfo()));

		// update the status in the internal DB
		logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.DELIVERED);

		// log the event
		for (String filename : logService.getFiles(receipt.getMessageId())) {
			protocolService.logDelivered(filename, receipt);
		}

		if (!logService.isTransparent(receipt.getMessageId())) {
			// and write the protocol
			for (String filename : logService.getFiles(receipt.getMessageId())) {
				writeDelivered(receipt, sentDir, filename);
			}
		} else {
			move(receipt);
		}
	}

	/**
	 * Do its stuff about any receipt that has a 500 code
	 *
	 * @param receipt         The receipt
	 * @param logService      The log service
	 * @param protocolService The protocol service
	 * @param sentDir         The directory for the sent stuff
	 * @throws LogServiceException Woops
	 */
	private void handle500Receipt(Receipt receipt, LogService logService, ProtocolService protocolService,
								  File sentDir) throws LogServiceException {
		LOG.info(MessageFormat.format("message ID {0} : the message could not be sent or delivered by the Sedex adapter, status '{1}'",
				receipt.getMessageId(), receipt.getStatusInfo()
		));

		MessageFormat.format("{0} et {1}", 1, 2);

		// update the status in the internal DB
		logService.setStatusChange(
				receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.ERROR);

		// log the event
		for (String filename : logService.getFiles(receipt.getMessageId())) {
			protocolService.logError(filename, receipt);
		}

		// and write the protocol
		if (!logService.isTransparent(receipt.getMessageId())) {
			for (String filename : logService.getFiles(receipt.getMessageId())) {
				writeError(receipt, sentDir, filename);
			}
		} else {
			move(receipt);
		}
	}

	/**
	 * Do its stuff about any receipt that has a 204 code
	 *
	 * @param receipt         The receipt
	 * @param logService      The log service
	 * @param protocolService The protocol service
	 * @param sentDir         The directory for the sent stuff
	 * @throws LogServiceException Woops
	 */
	private void handle204Receipt(Receipt receipt, LogService logService, ProtocolService protocolService,
								  File sentDir) throws LogServiceException {
		LOG.info(MessageFormat.format("message ID {0}: the message got expired by the Sedex " +
				"adapter, status '{1}'", receipt.getMessageId(), receipt.getStatusInfo()));

		// the message is expired; update the status in the internal DB
		logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()),
				LogStatus.EXPIRED);

		// log the event
		for (String filename : logService.getFiles(receipt.getMessageId())) {
			protocolService.logExpired(filename, receipt);
		}

		// and write the protocol
		if (!logService.isTransparent(receipt.getMessageId())) {
			for (String filename : logService.getFiles(receipt.getMessageId())) {
				writeExpired(receipt, sentDir, filename);
			}
		} else {
			move(receipt);
		}
	}

	/**
	 * Writes the protocol file after the given message was sent by the Sedex adapter.
	 *
	 * @param receipt the message that has been forwarded
	 * @param toDir   in which directory to create the protocol files
	 * @throws IllegalArgumentException if the provided <code>File</code> object is not
	 *                                  a directory
	 */
	@SuppressWarnings("unused")
	private void writeSent(Receipt receipt, File toDir, String filename) {

		Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + MSG_NOT_A_DIRECTORY);

		final String text = MessageFormat.format(PROTOCOL_FORMAT_NORMAL,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getEventDate(),
				"");

		// for each file in the receipt
		ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
	}


	/**
	 * Writes the protocol files after the given message was delivered by the Sedex adapter
	 * and there is the confirmation receipt.
	 *
	 * @param receipt the message that has been forwarded
	 * @param toDir   in which directory to create the protocol files
	 * @throws IllegalArgumentException if the provided <code>File</code> object is not
	 *                                  a directory
	 */
	private void writeDelivered(Receipt receipt, File toDir, String filename) {
		Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + MSG_NOT_A_DIRECTORY);

		final String text = MessageFormat.format(PROTOCOL_FORMAT_NORMAL,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getEventDate()
		);

		ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
	}

	/**
	 * Writes the protocol files after the given message was delivered by the Sedex adapter
	 * and there is the confirmation receipt.
	 *
	 * @param receipt the message that has been forwarded
	 * @param toDir   in which directory to create the protocol files
	 * @throws IllegalArgumentException if the provided <code>File</code> object is not
	 *                                  a directory
	 */
	private void writeExpired(Receipt receipt, File toDir, String filename) {
		Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + MSG_NOT_A_DIRECTORY);

		final String text = MessageFormat.format(PROTOCOL_FORMAT_EXPIRED,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getEventDate()
		);

		ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
	}


	/**
	 * Writes the protocol files after the given message was delivered by the Sedex adapter
	 * and there is the confirmation receipt.
	 *
	 * @param receipt the message that has been forwarded
	 * @param toDir   in which directory to create the protocol files
	 * @throws IllegalArgumentException if the provided <code>File</code> object is not
	 *                                  a directory
	 */
	private void writeError(Receipt receipt, File toDir, String filename) {
		Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + MSG_NOT_A_DIRECTORY);

		final String text = MessageFormat.format(PROTOCOL_FORMAT_ERROR,
				receipt.getMessageId(),
				receipt.getRecipientId(),
				receipt.getSentDate(),
				receipt.getStatusCode(),
				receipt.getStatusInfo()
		);

		ProtocolWriter.getInstance().writeProtocolError(toDir, filename, text);
	}

	/*
	 * (non-Javadoc)
	 * @see ch.admin.suis.msghandler.checker.TransparentStatusCheckerSession#move(ch.admin.suis.msghandler.common.Receipt)
	 */
	private void move(Receipt receipt) {
		if (null == receipt.getReceiptFile()) {
			// do nothing if the receipt is artificially created and is not based on an actual file
			return;
		}

		// checker configuration
		StatusCheckerConfiguration configuration = context.getClientConfiguration().getStatusCheckerConfiguration();

		for (ReceiptsFolder folder : configuration.getReceiptsFolders()) {
			if (StringUtils.equals(folder.getSedexId(), receipt.getSenderId()) && folder.isConfiguredFor(receipt.getMessageType())) {

				// both absolute and relative paths enabled for receipts directories (MANTIS 0004153)
				try {
					FileUtils.copy(receipt.getReceiptFile(), new File(folder.getDirectory(),
							receipt.getReceiptFile().getName()));

					LOG.info(MessageFormat.format(
							"the envelope file {0} successfully copied to the external directory {1}",
							receipt.getReceiptFile().getAbsolutePath(), folder.getDirectory().getAbsolutePath()));
				} catch (IOException e) {
					LOG.error(MessageFormat.format(
							"cannot copy the envelope file {0} to the external directory {1}",
							receipt.getReceiptFile().getAbsolutePath(), folder.getDirectory()), e);

					break;
				}

				// remove the original envelope file
				if (receipt.getReceiptFile().delete()) {
					LOG
							.debug(MessageFormat
									.format(
											"original envelope file {0} successfully removed",
											receipt.getReceiptFile().getAbsolutePath()));
				} else {
					LOG
							.error(MessageFormat
									.format(
											"cannot delete the original envelope file {0}",
											receipt.getReceiptFile().getAbsolutePath()));
				}

				return;
			}
		}
	}

}
