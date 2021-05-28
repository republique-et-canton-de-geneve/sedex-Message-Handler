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
import ch.admin.suis.msghandler.common.*;
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.log.LogServiceException;
import ch.admin.suis.msghandler.log.Mode;
import ch.admin.suis.msghandler.naming.NamingService;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * An implementation of the
 * <code>SenderSession</code> interface that puts outgoing messages to the ouput folder of the Sedex adapter without
 * creating the envelopes for them. This implementation expects that the Sedex envelopes and their data files are coming
 * in pairs and are transparently put to the Sedex adapter's outbox.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class TransparentSenderSessionImpl extends SenderSession implements ClientCommons {

  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
          .getLogger(TransparentSenderSessionImpl.class.getName());

  private List<Outbox> outboxes;

  /**
   * Creates a new transparent sender session for the given outboxes and the context.
   *
   * @param context the current state of the message handler
   * @param outboxes the outbox that should be checked during this session
   */
  public TransparentSenderSessionImpl(MessageHandlerContext context, List<Outbox> outboxes) {
    super(context);
    this.outboxes = outboxes;
  }

  /*
   * (non-Javadoc)
   *
   * @see ch.admin.suis.msghandler.sender.SenderSession#createMessages()
   */
  @Override
  public Collection<Message> createMessages() {
    final LogService logService = getContext().getLogService();
    final ProtocolService protocolService = getContext().getProtocolService();

    final ArrayList<Message> messages = new ArrayList<Message>();

    // for each outbox in this configuration
    for(Outbox outbox : outboxes) {
      // the outbox folder
//      final File outboxDir = FileUtils.createPath(context.getClientConfiguration().getBaseDir(), outbox.getDirectory());

      for(Message message : new MessageCollection(outbox.getDirectory()).get()) {
        // can we send this message?
        try{
            if(!logService.setSending(Mode.TRANSP, message.getRecipientIds(), message.getDataFile().getName())) {
              LOG.info(MessageFormat.format("file {0} is already sent to the recipient {1} ", new Object[]{
                message.getDataFile().getName(), message.getRecipientsAsString()}));
            }
        }
        catch(LogServiceException e){
          LOG.fatal("internal problem with the log service: " + e.getMessage());
          // this is a fatal problem caused by some underlying problem
          return Collections.emptyList(); // do not continue
        }

        // add to protocol
        protocolService.logPreparing(message.getDataFile().getAbsolutePath(), message);

        messages.add(message);
      }
    }

    return messages;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * ch.admin.suis.msghandler.sender.SenderSession#logSuccess(ch.admin.suis.
   * msghandler.common.Message)
   */
  @Override
  public void logSuccess(Message message) {
    final File file = message.getDataFile();

    // store in the internal DB
    try{
      getContext().getLogService().setForwarded(Mode.TRANSP, message.getRecipientIds(), file.getName(), message.getMessageId());
    }
    catch(LogServiceException e){
      LOG.fatal(MessageFormat.format(
              "cannot set the status to SENT in the internal DB for the file {0} and message ID {1}", new Object[]{
        file, message.getMessageId()}));
    }

    // create the log entry
    getContext().getProtocolService().logForwarded(file.getAbsolutePath(), message);

    // cleanup the temporary files
    cleanup(message);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * ch.admin.suis.msghandler.sender.SenderSession#send(ch.admin.suis.msghandler
   * .common.Message)
   */
  @Override
  public void sendImpl(Message message, File sedexOutputDir) throws IOException {

    // the data file name
    File dataFile = new File(sedexOutputDir, message.getDataFile().getName());
    // the envelope
    File envelopeFile = new File(sedexOutputDir, message.getEnvelopeFile().getName());

    // first, copy the data file
    try{
      FileUtils.copy(message.getDataFile(), dataFile);

      LOG.debug(MessageFormat.format("the data file {0} copied to the outbox {2} as {1}", //
              new Object[]{message.getDataFile().getAbsolutePath(), dataFile, sedexOutputDir.getAbsolutePath()}));
    }
    catch(IOException e){
      final String errorMessage = MessageFormat.format("cannot copy the data file {0} to the outbox {2} as {1}", //
              new Object[]{message.getDataFile().getAbsolutePath(), dataFile, sedexOutputDir.getAbsolutePath()});
      LOG.fatal(errorMessage);
      // signal a failure
      throw e;
    }

    // create the envelope
    try{
      FileUtils.copy(message.getEnvelopeFile(), envelopeFile);
      // ok
      LOG.info(MessageFormat.format("the data file and envelope for the message ID {0} copied to the outbox {1}", //
              new Object[]{message.getMessageId(), sedexOutputDir.getAbsolutePath()}));
    }
    catch(IOException e){
      final String errorMessage = MessageFormat.format("cannot move the envelope {0} to the outbox {1}", //
              new Object[]{message.getEnvelopeFile().getAbsolutePath(), sedexOutputDir.getAbsolutePath()});
      LOG.fatal(errorMessage);

      // signal a failure
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see ch.admin.suis.msghandler.sender.SenderSession#cleanup()
   */
  @Override
  public void cleanup() {
    // does nothing
  }
}
