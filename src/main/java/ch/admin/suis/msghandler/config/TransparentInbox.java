package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import static org.apache.log4j.Logger.getLogger;

/**
 * Inbox for transparent applications.
 *
 * @author $Author$
 * @version $Revision$
 */

public class TransparentInbox extends Inbox
{
  private static final Logger LOG = getLogger(TransparentInbox.class.getName());

  /**
   * @param directory
   *     the directory of the inbox
   * @param sedexId
   *     sedexId
   * @param types
   *     msgTypes to handle
   * @throws ConfigurationException
   */
  public TransparentInbox(final File directory, final String sedexId, final Collection<MessageType> types)
      throws ConfigurationException
  {
    super(directory, sedexId, types, Mode.Transparent);
  }

  @Override
  public void receive(final MessageHandlerContext context, final Message message)
      throws IOException
  {
    // does nothing since it is a transparent application (the data file and the envelope will be handled by the
    // application itself)
    LOG.info(String.format("Transparent msg received: msgType=%s, sender=%s, recipient=%s, msgId=%s",
        message.getMessageType(), message.getSenderId(), message.getRecipientIds(), message.getMessageId()));
  }

  @Override
  public void extract(final MessageHandlerContext context, final Message message)
      throws IOException
  {
    final ProtocolService protocolService = context.getProtocolService();
    final File envelope = new File(FileUtils.getFilename(getDirectory(), message.getEnvelopeFile().getName()));

    FileUtils.copy(message.getEnvelopeFile(), envelope);

    // if everything is ok
    LOG.info(MessageFormat.format("the envelope file {0} put transparently into the inbox {1}",
        message.getEnvelopeFile().getName(), getDirectory()));

    final File file = new File(FileUtils.getFilename(getDirectory(), message.getDataFile().getName()));
    FileUtils.copy(message.getDataFile(), file);

    // if everything is ok
    LOG.info(MessageFormat.format("the data file {0} put transparently into the inbox {1}",
        message.getDataFile().getName(), getDirectory()));

    // protocol
    protocolService.logReceiving(message.getDataFile().getName(), message);

  }
}
