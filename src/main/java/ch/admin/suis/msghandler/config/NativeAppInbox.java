package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ZipUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import static org.apache.log4j.Logger.getLogger;

/**
 * Inbox for native applications.
 *
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */

public class NativeAppInbox extends Inbox {
	private static final Logger LOG = getLogger(NativeAppInbox.class.getName());

	/**
	 * @param directory the directory of the inbox
	 * @param sedexId   sedexId
	 * @param types     msgTypes to handle
	 * @throws ConfigurationException Config problems...
	 */
	public NativeAppInbox(final File directory, final String sedexId,
						  final Collection<MessageType> types) throws ConfigurationException {
		super(directory, sedexId, types, Mode.NATIVE);
	}

	@Override
	public void receive(final MessageHandlerContext context, final Message message)
			throws IOException {

	        LOG.info(String.format("Native msg received: msgType=%s, sender=%s, recipient=%s, msgId=%s",
		         message.getMessageType(), message.getSenderId(), message.getRecipientIds(), message.getMessageId()));

		final ClientConfiguration clientConfig = context.getClientConfiguration();
		final File corruptedDir = new File(new File(clientConfig.getWorkingDir()), ClientCommons.CORRUPTED_DIR);

		if (message.getFiles().isEmpty()) {
			// move to the corrupted???
			FileUtils.moveFile(message.getEnvelopeFile(), new File(corruptedDir, message.getEnvelopeFile().getName()));
			FileUtils.moveFile(message.getDataFile(), new File(corruptedDir, message.getDataFile().getName()));

			// if everything is ok
			LOG.info(String.format("Native msg (msgId=%s): file %s received, but put into the corrupted directory %s",
			         message.getMessageId(), message.getDataFile().getName(), corruptedDir));
		} else {
			// move the file in the message to the inbox
			for (File incomingFile : message.getFiles()) {
				// this file is extracted to a temporary directory - we may move it (renaming if needed)
			        String destFilename = FileUtils.copyIntoDirectory(incomingFile, getDirectory());

				// if everything is ok
			        LOG.info(String.format("Native msg (msgId=%s): file %s received, put into inbox as %s",
			                message.getMessageId(),
			                incomingFile.getName(),
			                destFilename));
			}
		}

	}

	@Override
	public void extract(final MessageHandlerContext context, final Message message)
			throws IOException {

		final ProtocolService protocolService = context.getProtocolService();
		final ClientConfiguration clientConfig = context.getClientConfiguration();

		// temporary directory to store the incoming files
		final File inboxTmpDir = new File(new File(clientConfig.getWorkingDir()), ClientCommons.INBOX_TMP_DIR);

		LOG.debug("unpacking message with message_ID=" + message.getMessageId());
		try {
			for (File file : ZipUtils.decompress(message.getDataFile(), inboxTmpDir)) {
				protocolService.logReceiving(file.getName(), message);

				message.addFile(file);
				LOG.debug("extracted file: " + file);
			}
		} catch (IOException e) { // corrupted ZIP file
			LOG.error(
					MessageFormat
							.format(
									"the file {0} is corrupted or is not a ZIP file and will be moved to the 'corrupted' directory",
									message.getDataFile().getAbsolutePath()), e);
		}

	}
}
