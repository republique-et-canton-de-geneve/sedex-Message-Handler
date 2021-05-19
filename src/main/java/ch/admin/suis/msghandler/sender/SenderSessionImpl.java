/*
 * $Id$
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
package ch.admin.suis.msghandler.sender;

import ch.admin.suis.msghandler.config.Outbox;
import ch.admin.suis.msghandler.config.SigningOutbox;
import ch.admin.suis.msghandler.common.*;
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.log.LogServiceException;
import ch.admin.suis.msghandler.log.Mode;
import ch.admin.suis.msghandler.log.ProtocolWriter;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.signer.Signer;
import ch.admin.suis.msghandler.signer.SignerException;
import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ISO8601Utils;
import ch.admin.suis.msghandler.util.ZipUtils;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xml.sax.SAXException;

/**
 * An implementation of the <code>SenderSession</code> interface that puts
 * outgoing messages to the ouput folder of the Sedex adapter creating the
 * envelopes for them.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class SenderSessionImpl extends SenderSession implements ClientCommons {
	/** logger */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SenderSessionImpl.class.getName());

	private List<Outbox> outboxes;

	/**
	 * the messages that were not sent for some reason.
	 */
	private List<Message> notSent = new ArrayList<Message>();

	/**
	 * Creates a new sender session for the given outboxes and the context.
	 *
	 * @param context
	 *          the current state of the message handler
	 * @param outboxes
	 *          the outbox that should be checked during this session
	 */
	public SenderSessionImpl(MessageHandlerContext context, List<Outbox> outboxes) {
		super(context);
		this.outboxes = outboxes;
	}

	/**
	 * Creates the ZIP files for each recipient detected from the files residing
	 * in the outbox configured for this session.
	 *
	 * @see ch.admin.suis.msghandler.sender.SenderSession#createMessages()
	 */
	@Override
	public Collection<Message> createMessages() {
		// default sender ID
		final String defaultSenderId = getContext().getClientConfiguration()
				.getSedexAdapterConfiguration().getParticipantId();

		final LogService logService = getContext().getLogService();
		final ProtocolService protocolService = getContext().getProtocolService();
		// a table to store the mapping between pairs of type (recipient ID, message
		// type) and the messages
		final HashMap<Integer, Message> pairs = new HashMap<Integer, Message>();
		// the temporary outbox
		final File outboxTmpDir = new File(new File(getContext()
				.getClientConfiguration().getWorkingDir()), OUTBOX_TMP_DIR);

		ArrayList<Message> messages = new ArrayList<Message>();

		// for each outbox in this configuration
		for (Outbox outbox : outboxes) {
			// the outbox folder
//			final File outboxDir = FileUtils.createPath(context.getClientConfiguration().getBaseDir(), outbox.getDirectory());

			handleSigning(outbox.getSigningOutboxes(), outbox.getDirectory());

			// check for the files over there
      File[] files = FileUtils.listFiles(outbox.getDirectory(), FileFilters.ALL_FILES_FILTER);

			for (File file : files) {
				// determine the recipient ID (only via the script)
				final String participantId = outbox.getParticipantIdResolver().resolve(
						file.getAbsolutePath());

				if (StringUtils.isEmpty(participantId)) {
					LOG.fatal("cannot determine recipient ID for the file "
							+ file.getAbsolutePath());
					continue; // but try another file anyway
				}

				// can we send this message?
				try {
					if (!logService.setSending(Mode.MH, participantId, file.getName())) {
						LOG.info(MessageFormat.format(
								"file {0} is already sent to the recipient {1} ", new Object[] {
										file.getName(), participantId }));
						// try another file
						continue;
					}
				}
				catch (LogServiceException e) {
					LOG.fatal("internal problem with the log service: " + e.getMessage());
					// this is a fatal problem caused by some underlying problem
					break; // do not continue
				}

				// determine the message type
				final MessageType type = outbox.getType();

				// determine the sender ID
				String senderId = outbox.getSedexId();
				if (StringUtils.isBlank(senderId)) {
					// in vain, take default
					senderId = defaultSenderId; // as in the setup

					LOG
							.debug("using the sedex ID to determine the sender ID for the file "
									+ file.getAbsolutePath());
				}
				else {
					LOG
							.debug("using the sender's sedex ID attribute to determine the sender ID for the file "
									+ file.getAbsolutePath());
				}

				int hashCode = new HashCodeBuilder().append(participantId).append(type)
						.append(senderId).toHashCode();
				Message message = pairs.get(hashCode);
				if (null == message) {
					message = new Message();
					pairs.put(hashCode, message); // set the message

					message.setMessageType(type); // message type
          message.addRecipientId(participantId);// the recipient
					message.setMessageId(UUID.randomUUID().toString()); // generate the
					// message ID
					message.setMessageClass("0"); // initial message
					message.setSenderId(senderId);

					message.setMessageDate(ISO8601Utils.format(new Date()));
					// TODO how should we set the event time while creating a message?
					message.setEventDate(ISO8601Utils.format(new Date()));
				}

				// log the event
				protocolService.logPreparing(file.getAbsolutePath(), message);

				// add this file to the message
				message.addFile(file);

				LOG.info(MessageFormat.format(
						"message ID {1}: preparing to send the file {0}", new Object[] {
								file, message.getMessageId() }));
			}

		}

		// END

    // for each message: create the ZIP and the envelope
    for(Message message : pairs.values()) {
      // create the envelope file
      try{
        message.setEnvelopeFile(createEnvelope(outboxTmpDir, message));
      }
      catch(IOException e1){
        // the file cannot be created - skip this message
        for(String recipient : message.getRecipientIds()) {
          LOG.error(MessageFormat.format("cannot create an envelope file for the recipient {0} and message type {1}",
                  new Object[]{recipient, message.getMessageType()}), e1);
        }

        // add the message to the not_sent collection, so that can be cleanup
        // later
        notSent.add(message);
        continue; // try another message
      }
      catch(SAXException e2){
        // something wrong in the data provided by the user (recipient ID?) -
        // skip this message
        for(String recipient : message.getRecipientIds()) {
          LOG.error(MessageFormat.format(
                  "cannot create a valid envelope file for the recipient {0} and message type {1}: ",
                  new Object[]{recipient, message.getMessageType()}), e2);
        }
        // add the message to the not_sent collection, so that can be cleanup
        // later
        notSent.add(message);

        continue; // try another message
      }

			// we create the ZIP here
			try {
				message.setDataFile(ZipUtils.compress(outboxTmpDir, message.getFiles()));
			}
			catch (IOException e) {
				// the file cannot be created - skip this message
        for(String recipient : message.getRecipientIds()){
				LOG.error(MessageFormat.format("cannot create a ZIP file for the recipient {0} and message type {1}",
												new Object[] { recipient,
														message.getMessageType() }), e);
        }
				// add the message to the not_sent collection, so that can be cleanup
				// later
				notSent.add(message);

				continue; // try another message
			}

			// if everything ok, then add the message to the result
			messages.add(message);

			LOG.info(MessageFormat.format(
					"the message {0} is ready to be forwarded to the Sedex adapter",
					new Object[] { message.getMessageId() }));
		}

		return messages;
	}

	/**
	 * Signs all files in the signing outbox directories. The signed PDFs will be
	 * stored in the normal MessageHandler output directory.
	 * <p />
	 * The signing process will never overwrite existing files. By a filename
	 * conflict the signed PDF will be renamed with the MHs standard procedure.
	 * <p />
	 * Logging and Exception handling is completely implemented in this method.
	 *
	 * @param signingOutboxes
	 *          The signingOutboxes which will be processed.
	 * @param destDir
	 *          The normal MessageHandler output directory.
	 */
	private void handleSigning(List<SigningOutbox> signingOutboxes, File destDir) {
		try {
			if (signingOutboxes == null || signingOutboxes.isEmpty()) {
				LOG.debug("Nothing to sign. No SigningOutbox is defined for: "
						+ destDir.getName());
				return;
			}
			else {
				LOG.debug("For " + destDir.getName() + " are " + signingOutboxes.size()
						+ " SigningOutboxes defined");
			}

            final File corruptedDir = new File(new File(this.getContext().getClientConfiguration().getWorkingDir()),
              ClientCommons.CORRUPTED_DIR);
			Signer signer = new Signer(signingOutboxes, destDir, corruptedDir);
			List<File> signedFiles = signer.sign();
			signer.cleanUp(signedFiles);
		}
		catch (SignerException ex) {
			LOG.fatal("Not able to sign PDFs. " + ex.getMessage(), ex);
		}catch(ConfigurationException ex){
          LOG.fatal("Unable to reload config: " + ex.getMessage(), ex);
        }
		catch (Exception ex) {
			LOG.fatal("Not able to sign PDFs. Unknown exception! " + ex.getMessage(),
					ex);
		}
	}

	/**
	 * Moves the created data file and envelope to the output folder of the Sedex
	 * adapter.
	 *
	 * @see ch.admin.suis.msghandler.sender.SenderSession#send(ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void sendImpl(Message message, File sedexOutputDir) throws IOException {

		// the data file name
		File dataFile = new File(sedexOutputDir, FileUtils.getDataFilename(message
				.getMessageId()));
		// the envelope (note, this does not create a file)
		File envelopeFile = new File(sedexOutputDir, FileUtils.getEnvelopeFilename(message.getMessageId()));

		// first, move the data file
		try {
			FileUtils.copy(message.getDataFile(), dataFile);

			LOG.debug(MessageFormat.format("the data file {0} copied to the outbox of the sedex adapter as {1}",
					new Object[] { message.getDataFile().getAbsolutePath(), dataFile }));
		}
		catch (IOException e) {
			final String errorMessage = MessageFormat.format("cannot copy the data file {0} to the outbox of the sedex adapter as {1}",
							new Object[] { message.getDataFile().getAbsolutePath(), dataFile });
			LOG.fatal(errorMessage);
			throw e;
		}

		// create the envelope
		try {
			FileUtils.copy(message.getEnvelopeFile(), envelopeFile);
			// ok
			LOG.info(MessageFormat.format("message ID {0} : the data file and envelope copied to the outbox of the sedex adapter",
									new Object[] { message.getMessageId() }));
		}
		catch (IOException e) {
			final String errorMessage = MessageFormat.format("cannot move the envelope {0} to the outbox of the sedex adapter {1}",
							new Object[] { message.getEnvelopeFile().getAbsolutePath(), envelopeFile });
			LOG.fatal(errorMessage);
			throw e;
		}
	}

	/**
	 * Removes the temporary files remained from the messages in this session.
	 *
	 * @see ch.admin.suis.msghandler.sender.SenderSession#commit()
	 */
	@Override
	public void cleanup() {
		for (Message message : notSent) {
			cleanup(message);
		}
	}

	/**
	 * Moves the files the message consists of into the <code>sent</code>
	 * directory and writes the protocol file.
	 *
	 * @see ch.admin.suis.msghandler.sender.SenderSession#logSuccess(ch.admin.suis.msghandler.common.Message)
	 */
	@Override
	public void logSuccess(Message message) {
		File sentDir = new File(
				new File(getContext().getClientConfiguration().getWorkingDir()), SENT_DIR);
		for (File file : message.getFiles()) {
			// store in the internal DB
			try {
				// an error fixed (recipient ID should have been set here and not the sender ID)
				getContext().getLogService().setForwarded(Mode.MH, message.getRecipientIds(), file.getName(), message.getMessageId());
			}
			catch (LogServiceException e) {
				LOG
						.fatal(MessageFormat
								.format(
										"message ID {1}: cannot set the status to SENT in the internal DB for the file {0}",
										new Object[] { file, message.getMessageId() }));
			}

			// create the log entry
			getContext().getProtocolService()
					.logForwarded(file.getAbsolutePath(), message);

			// move to the sent directory
			File destFile = new File(sentDir, file.getName());
      try{
        FileUtils.moveFile(file, destFile);
        LOG.debug("file moved to SENT: " + file.getAbsolutePath());
      }
      catch(IOException ex){
        LOG.error("Unable to move file. Src: " +  file.getAbsolutePath() + ", dest: " + destFile, ex);
      }

			writeForwarded(message, sentDir, file.getName());
		}

		// cleanup the temporary files
		cleanup(message);
	}

	private File createEnvelope(File outboxDir, Message message)
			throws IOException, SAXException {
		final File envelopeFile = File.createTempFile("envl", ".xml", outboxDir);

		Writer writer = null;
		boolean exceptionThrown = false;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					envelopeFile), "UTF-8"));
			// let the message serialize itself
			message.writeEnvelope(writer);

			return envelopeFile;
		}
		catch (IOException e) {
			LOG.error("cannot create the file " + envelopeFile.getAbsolutePath(), e);
			exceptionThrown = true;

			throw e;
		}
		catch (SAXException e) {
			LOG.error("cannot create the file " + envelopeFile.getAbsolutePath(), e);
			exceptionThrown = true;

			throw e;
		}
		finally {
			if (null != writer) {
				try {
					writer.close();
				}
				catch (IOException e) {
					LOG.error("cannot properly close the output stream for "
							+ envelopeFile.getAbsolutePath(), e);
				}
			}

			if (exceptionThrown && !envelopeFile.delete()) {
				LOG.error("cannot remove the temporary envelope file "
						+ envelopeFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Writes the protocol files after the given message was forwarded.
	 *
	 * @param message
	 *          the message that has been forwarded
	 * @param toDir
	 *          in which directory to create the protocol files
	 * @throws IllegalArgumentException
	 *           if the provided <code>File</code> object is not a directory
	 */
	private void writeForwarded(Message message, File toDir, String filename) {
		Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath()
				+ " is not a directory");

    for(String recipientId : message.getRecipientIds()) {
      final String text = MessageFormat.format(PROTOCOL_FORMAT_NORMAL, new Object[]{message.getMessageId(), recipientId,
        message.getMessageDate(), ""});
      ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
    }
	}
}
