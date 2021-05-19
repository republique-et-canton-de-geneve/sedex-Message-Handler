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

package ch.admin.suis.msghandler.checker;

import ch.admin.suis.msghandler.common.ClientCommons;
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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.xml.sax.SAXException;

/**
 * The implementation of the <code>StatusCheckerSession</code> interface for the
 * Sedex adapter.
 *
 * @author      Alexander Nikiforov
 * @author      $Author$
 * @version     $Revision$
 */
public class StatusCheckerSessionImpl implements StatusCheckerSession, ClientCommons {
  /** logger */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
      .getLogger(StatusCheckerSessionImpl.class.getName());

  private MessageHandlerContext context;

  /**
   * Creates a new instance of this class.
   *
   * @param context
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
   * The list of the found receipts is then returned. If there are no receipts, this
   * method returns an empty collection.
   *
   * @see ch.admin.suis.msghandler.checker.StatusCheckerSession#getMessagesIds()
   */
  @Override
  public Collection<Receipt> getMessagesIds() throws LogServiceException {
    ArrayList<Receipt> receipts = new ArrayList<Receipt>();

    // the internal DB
    final LogService logService = context.getLogService();

    // the Sedex adapter's receipt directory
    File receiptsDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getReceiptDir());

    // get the messages that have either FORWARDED or SENT as their status
    TreeSet<String> sentIds = new TreeSet<String>(logService.getSentMessages());

    // loop over the files in the receipts directory
    // check for the files over there
    File[] files = receiptsDir.listFiles(FileFilters.XML_FILTER);

    if (null == files) {
      LOG.error("an I/O error occured while reading the receipts from the Sedex adapter; " +
          "check the message handler configuration to see whether the specified 'receipts' directory " +
          "for the Sedex Adapter actually exists");

      return Collections.emptyList();
    }

    //
    ArrayList<String> toBeRemoved = new ArrayList<String>();
    // for each receipt found
    for (File file : files) {
      InputStream reader = null;
      try {
        reader = new FileInputStream(file);
        Receipt receipt = Receipt.createFrom(reader);
        if (sentIds.contains(receipt.getMessageId())) {
          // get the sent date for this receipt (it is not unfortunately in the receipt XML)
          receipt.setSentDate(ISO8601Utils.format(logService.getSentDate(receipt.getMessageId())));
          receipt.setReceiptFile(file);
          // add it now
          receipts.add(receipt);
          LOG.info(MessageFormat.format("message ID {0}: receipt found", receipt.getMessageId()));
          // set to remove the id from the tree
          toBeRemoved.add(receipt.getMessageId());
        }
      }
      catch (FileNotFoundException e) {
        LOG.error("cannot find the file " + file.getAbsolutePath() + "; is it already removed?", e);
        continue;
      }
      catch (IOException e) {
        LOG.error("cannot read the file " + file.getAbsolutePath(), e);
        continue;
      }
      catch (SAXException e) {
        LOG.error("cannot parse the file " + file.getAbsolutePath(), e);
        continue;
      }
      finally {
        if (null != reader) {
          try {
            reader.close();
          }
          catch (IOException e) {
            // ignore
          }
        }
      }
    }

    // remove from the list
    sentIds.removeAll(toBeRemoved);

    // now, lets look at what has remained to find out, whether the Sedex adapter has just sent the files
    // but not received the receipts (look only at forwarded messages that are not "transparent")
    final File outputDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getOutputDir());
//    final File sentDir = new File(context.getClientConfiguration().getSedexAdapterConfiguration().getSentDir());
    for (final String messageId : logService.getMessages(LogStatus.FORWARDED)) {
      if (sentIds.contains(messageId) && !logService.isTransparent(messageId) && !new File(outputDir, FileUtils.getDataFilename(messageId)).exists()) {
        // the envelope that we have created
        final Message message = getSentMessage(messageId);
        if (null != message) {
          // the file is send by the adapter but there is no receipt yet
          for(final String recipientId : message.getRecipientIds()) {
            receipts.add(new Receipt() {
              // we create a receipt object ourselves, just setting the status code to 0
              {
                setEventDate(ISO8601Utils.format(new Date())); // now
                setMessageId(messageId);
                setStatusCode(0); // we do not have any, right?
                setStatusInfo("the message is sent by the adapter; no receipt yet");
                setSentDate(message.getEventDate()); // find out when it was sent
                setRecipientId(recipientId); // find out whom it was sent to
                // the receipt file remains null
              }
            });
          }
          LOG.info("message has been sent by the Sedex adapter: " + messageId);
        }
        else {
          LOG.warn(MessageFormat.format("message ID {0}: message sent by the Sedex adapter, but there is no envelope in the Sedex sent directory", new Object[] {
              messageId}));
        }

        // remove the id from the tree
        sentIds.remove(messageId);
      }
    }
    // TODO sort out the receipts so that we can reliably process the situation where
    // there is more than one receipt pro message
    return receipts;
  }

  /**
   * Returns a message object for the given message ID by reading
   * the envelope file in the specified directory
   *
   * @param outputDir where to look for the envelope
   * @param messageId the message ID
   *
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
    if(!envelope.exists()) {
      LOG.warn("Sedex Sent directory does not contain file: " + envelope.getAbsolutePath()
              + ". Maybe Sedex moved the data file before the env file.");
      if(LOG.isDebugEnabled()) {
        String msg = "cannot read the envelope file " + envelope.getAbsolutePath();
        LOG.error(msg, new FileNotFoundException(msg));
      }
      return null;
    }

    InputStream reader = null;
    try {
      reader = new FileInputStream(envelope);
      return Message.createFrom(reader);
    }
    catch (IOException e) {
      LOG.error("cannot read the envelope file " + envelope.getAbsolutePath(), e);
      return null;
    }
    catch (SAXException e) {
      LOG.error("cannot parse the envelope file " + envelope.getAbsolutePath(), e);
      return null;
    }
    finally {
      if (null != reader) {
        try {
          reader.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }

  }


  /**
   * Updates the status for the message corresponding to this receipt.
   *
   * @see ch.admin.suis.msghandler.checker.StatusCheckerSession#updateStatus(java.lang.String)
   */
  @Override
  public void updateStatus(Receipt receipt) throws LogServiceException {
    // the internal DB
    final LogService logService = context.getLogService();

    final ProtocolService protocolService = context.getProtocolService();

    File sentDir = new File(new File(context.getClientConfiguration().getWorkingDir()), SENT_DIR);

    switch (receipt.getStatusCode()) {
      case 0:
        // that is what we has set; sent

        // update the status in the internal DB
        logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.SENT);

        // log the event
        // for each file in the message
        // the getFiles can throw a LogServiceException
        for (String filename : logService.getFiles(receipt.getMessageId())) {
          protocolService.logSent(filename, receipt);
        }

        // and write the protocol
        // writeSent(receipt, sentDir, filename);

        break;
      case 100:
        // everything is ok; mark as delivered
        LOG.info(MessageFormat.format("message ID {0}: the message has been delivered by the Sedex adapter, status {1}", new Object[] {
            receipt.getMessageId(), receipt.getStatusInfo() }));

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
        }
        else {
          move(receipt);
        }
        break;

      case 200:
        // invalid envelope syntax? error
      case 201:
        // duplicate message id (this should not happen!); write a fatal log entry
      case 202:
        // no data file; write a fatal log entry
      case 203:
        // the message date is too old
      case 300:
      case 301:
        // unknown recipient; error
      case 302:
        // we are unknown to the sedex adapter! error
      case 303:
        // the message type is unknown; error
      case 304:
        // the message class is unknown; error
      case 310:
        // the message is refused by the recipient; error
      case 311:
        // the message is refused, because we are not allowed to send to this recipient; error
      case 312:
        // the certificate is not valid; error
      case 313:
        // other recipient are not allowed to receive
      case 330:
        // the message size exceeds limit
      case 400:
        // network problem; error?
      case 401:
        // the hub is unreachable; error
      case 402:
        // the Sedex' directory is unreachable; error
      case 403:
        // logging service not reachable
      case 404:
        // authorization service not reachable
      case 500:
        // internal adapter error; error

        LOG.info(MessageFormat.format("message ID {0} : the message could not be sent or delivered by the Sedex adapter, status '{1}'", new Object[] {
            receipt.getMessageId(), receipt.getStatusInfo()
        }));

        // update the status in the internal DB
        logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.ERROR);

        // log the event
        for (String filename : logService.getFiles(receipt.getMessageId())) {
          protocolService.logError(filename, receipt);
        }

        // and write the protocol
        if (!logService.isTransparent(receipt.getMessageId())) {
          for (String filename : logService.getFiles(receipt.getMessageId())) {
            writeError(receipt, sentDir, filename);
          }
        }
        else {
          move(receipt);
        }
        break;

      case 320:
      case 204:
        LOG.info(MessageFormat.format("message ID {0}: the message got expired by the Sedex adapter, status '{1}'", new Object[] {
            receipt.getMessageId(), receipt.getStatusInfo()
        }));
        // the message is expired; expired
        // update the status in the internal DB
        logService.setStatusChange(receipt.getMessageId(), ISO8601Utils.parse(receipt.getEventDate()), LogStatus.EXPIRED);

        // log the event
        for (String filename : logService.getFiles(receipt.getMessageId())) {
          protocolService.logExpired(filename, receipt);
        }

        // and write the protocol
        if (!logService.isTransparent(receipt.getMessageId())) {
          for (String filename : logService.getFiles(receipt.getMessageId())) {
            writeExpired(receipt, sentDir, filename);
          }
        }
        else {
          move(receipt);
        }
        break;

      case 601:
        LOG.info(MessageFormat.format("message ID {0}: the Sedex adapter has successfully transferred the message to the intermediary server, status '{1}'", new Object[] {
            receipt.getMessageId(), receipt.getStatusInfo()
        }));
        // the message was transfered
        // TODO implement the functionality when the message is transferred
        break;

    }

  }

  /**
   * Writes the protocol file after the given message was sent by the Sedex adapter.
   *
   * @param receipt the message that has been forwarded
   * @param toDir in which directory to create the protocol files
   * @throws LogServiceException
   * @throws IllegalArgumentException if the provided <code>File</code> object is not
   * a directory
   */
  @SuppressWarnings("unused")
  private void writeSent(Receipt receipt, File toDir, String filename) {

    Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + " is not a directory");

    final String text = MessageFormat.format(PROTOCOL_FORMAT_NORMAL, new Object[] {
        receipt.getMessageId(),
        receipt.getRecipientId(),
        receipt.getEventDate(),
        "" });

    // for each file in the receipt
    ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
  }


  /**
   * Writes the protocol files after the given message was delivered by the Sedex adapter
   * and there is the confirmation receipt.
   *
   * @param receipt the message that has been forwarded
   * @param toDir in which directory to create the protocol files
   * @throws IllegalArgumentException if the provided <code>File</code> object is not
   * a directory
   */
  private void writeDelivered(Receipt receipt, File toDir, String filename) {
    Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + " is not a directory");

    final String text = MessageFormat.format(PROTOCOL_FORMAT_NORMAL, new Object[] {
        receipt.getMessageId(),
        receipt.getRecipientId(),
        receipt.getSentDate(),
        receipt.getEventDate()
        });

    ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
  }

  /**
   * Writes the protocol files after the given message was delivered by the Sedex adapter
   * and there is the confirmation receipt.
   *
   * @param receipt the message that has been forwarded
   * @param toDir in which directory to create the protocol files
   * @throws IllegalArgumentException if the provided <code>File</code> object is not
   * a directory
   */
  private void writeExpired(Receipt receipt, File toDir, String filename) {
    Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + " is not a directory");

    final String text = MessageFormat.format(PROTOCOL_FORMAT_EXPIRED, new Object[] {
        receipt.getMessageId(),
        receipt.getRecipientId(),
        receipt.getSentDate(),
        receipt.getEventDate()
        });

    ProtocolWriter.getInstance().writeProtocol(toDir, filename, text);
  }


  /**
   * Writes the protocol files after the given message was delivered by the Sedex adapter
   * and there is the confirmation receipt.
   *
   * @param receipt the message that has been forwarded
   * @param toDir in which directory to create the protocol files
   * @throws LogServiceException
   * @throws IllegalArgumentException if the provided <code>File</code> object is not
   * a directory
   */
  private void writeError(Receipt receipt, File toDir, String filename) {
    Validate.isTrue(toDir.isDirectory(), toDir.getAbsolutePath() + " is not a directory");

    final String text = MessageFormat.format(PROTOCOL_FORMAT_ERROR, new Object[] {
        receipt.getMessageId(),
        receipt.getRecipientId(),
        receipt.getSentDate(),
        receipt.getStatusCode(),
        receipt.getStatusInfo()
        });

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
//        File dest = FileUtils.createPath(context.getClientConfiguration().getBaseDir(), folder.getDirectory());
        try {
          FileUtils.copy(receipt.getReceiptFile(), new File(folder.getDirectory(), receipt.getReceiptFile().getName()));

          LOG.info(MessageFormat.format(
              "the envelope file {0} successfully copied to the external directory {1}", new Object[] {
                  receipt.getReceiptFile().getAbsolutePath(), folder.getDirectory().getAbsolutePath()}));
        }
        catch (IOException e) {
          LOG.error(MessageFormat.format(
              "cannot copy the envelope file {0} to the external directory {1}", new Object[] {
                  receipt.getReceiptFile().getAbsolutePath(), folder.getDirectory()}), e);

          // break
          break;
        }

        // remove the original envelope file
        if (receipt.getReceiptFile().delete()) {
          LOG
              .debug(MessageFormat
                  .format(
                      "original envelope file {0} successfully removed",
                      new Object[] { receipt.getReceiptFile().getAbsolutePath() }));
        }
        else {
          LOG
          .error(MessageFormat
              .format(
                  "cannot delete the original envelope file {0}",
                  new Object[] { receipt.getReceiptFile().getAbsolutePath() }));
        }

        return;
      }
    }
  }

}
