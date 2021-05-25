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

import ch.admin.suis.msghandler.common.LocalRecipient;
import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.log.LogServiceException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.admin.suis.msghandler.util.V2MessageXmlGenerator;
import ch.admin.suis.msghandler.util.V2ReceiptXmlGenerator;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * The
 * <code>SenderSession</code> defines operations that should perform a sender to pass the files from the output folder
 * to the Sedex adapter.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public abstract class SenderSession {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SenderSession.class.getName());

  private final MessageHandlerContext context;

  private static int sequenceNbr = 0;
  public static V2MessageXmlGenerator msgGen;

  public SenderSession(MessageHandlerContext context) {
    this.context = context;
    msgGen = new V2MessageXmlGenerator();
  }

  MessageHandlerContext getContext() {
    return context;
  }

  /**
   * Writes the log entry to the protocol system upon getting an error while trying to send the message.
   *
   * @param message TODO
   * @param e execption that has led to the error to be logged
   *
   * @throws IOException
   */
  final void logError(Message message, Exception e) {
    // cleanup the temporary files
    cleanup(message);
  }

  /**
   * Removes the remaining data and envelope files for this message.
   *
   */
  void cleanup(Message message) {
    // envelope
    if(null != message.getEnvelopeFile() && message.getEnvelopeFile().exists() && message.getEnvelopeFile().delete()) {
      LOG.debug(MessageFormat.format("envelope file {0} successfully removed", new Object[]{message.getEnvelopeFile()}));
    }

    // data file
    if(null != message.getDataFile() && message.getDataFile().exists() && message.getDataFile().delete()) {
      LOG.debug(MessageFormat.format("data file {0} successfully removed", new Object[]{message.getDataFile()}));
    }
  }

  /**
   * Returns the semaphore that should be acquired if the client wants to perform an operation without being interrupted
   * by the message handler.
   *
   * @return
   */
  Semaphore getDefenseLock() {
    return getContext().getDefenseLock();
  }

  private File getOutboxDirectory(String recipientId, int msgType) {
    File file;
    if(isLocalRecipient(recipientId, msgType)) {
      file = new File(getContext().getClientConfiguration().getSedexAdapterConfiguration().getInputDir());
    }
    else {
      file = new File(getContext().getClientConfiguration().getSedexAdapterConfiguration().getOutputDir());
    }
    return file;
  }

  private boolean isLocalRecipient(String recipientId, int msgType) {
    LocalRecipient lr = getContext().getClientConfiguration().getLocalRecipients().get(recipientId);
    return lr != null && lr.containsMsgType(msgType);
  }

  /**
   * Saves a receipt file for a given message. Filename:
   * http://www.bfs.admin.ch/bfs/portal/de/index/news/00/00/12/01.parsys.73253.downloadList.78528.DownloadFile.tmp/sedexhandbuchv4.0.315.11.2012.pdf
   * page 27
   *
   * @param message
   */
  private void saveReceipt(Message message, String recipientId) {
    String receipt;
    V2ReceiptXmlGenerator recGen = new V2ReceiptXmlGenerator();
    try{
      receipt = recGen.generateSuccess(message, recipientId);
    }
    catch(SAXException | IOException | ParseException | DatatypeConfigurationException ex){
      LOG.error("Unable to generate the receipt xml file. ex: " + ex.getMessage(), ex);
      return;
    }

    String directory = getContext().getClientConfiguration().getSedexAdapterConfiguration().getReceiptDir();
    File dstFile = null;

    while (dstFile == null || dstFile.exists()) {
      sequenceNbr++;
      String fileName = "receipt__ID_" + message.getMessageId() + "_" + sequenceNbr + ".xml";
      dstFile = new File(directory, fileName);
    }

    try{
      LOG.debug("Generate and save a receipt report: " + dstFile.getAbsolutePath());
      FileUtils.write(dstFile, receipt, "UTF-8");
    }
    catch(IOException ex){
      LOG.error("Unable to save the receipt file. " + dstFile.getAbsolutePath() + ", ex: " + ex.getMessage(), ex);
    }
  }

  final void send(Message message) throws IOException {
    int msgType = message.getMessageType().getType();
    Set<String> outboxDirs = new HashSet<>();

    for(String recipientId : message.getRecipientIds()) {
      final File sedexOutputDir = getOutboxDirectory(recipientId, msgType);

      /* Hack Mehrere recipientIds in einer Message: Wenn mehrere Empf�nger vorhanden sein sollten, darf pro Verzeichnis
       * nur einmal die Datei abgelegt werden. Die Recipients sind ja im envelope definiert. "sendImpl" ist im Grunde
       * nur ein Kopiervorgang.
       */
      if(!outboxDirs.contains(sedexOutputDir.getAbsolutePath())) {
        outboxDirs.add(sedexOutputDir.getAbsolutePath());

        sendImpl(message, sedexOutputDir);
      }

      if(isLocalRecipient(recipientId, msgType)) {
        saveReceipt(message, recipientId);
      }
    }
  }

  /**
   * Creates a collection of messages to be sent during the sender session.
   *
   * @return collection of messages to be sent or an empty collection if there is nothing to be sent
   */
  abstract Collection<Message> createMessages();

  /**
   * Passes the message to the Sedex adapter.
   *
   * @param message
   * @throws IOException if the message cannot be passed to the Sedex adapter because of an IO error
   */
  abstract void sendImpl(Message message, File outboxDir) throws IOException;

  /**
   * Releases the resources used in this session.
   */
  abstract void cleanup();

  /**
   * Writes the log entry to the protocol system upon succeeding to send the file.
   *
   * @param message the message that have been successfully sent
   *
   * @throws IOException
   */
  abstract void logSuccess(Message message) throws LogServiceException;
}
