/*
 * $Id: DecryptingInbox.java 349 2015-08-21 14:27:30Z sasha $
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
package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ZipUtils;
import ch.glue.fileencryptor.CryptographyException;
import ch.glue.fileencryptor.Decryptor;
import ch.glue.fileencryptor.EntryNameResolver;
import ch.glue.fileencryptor.InvalidContainerException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import static java.text.MessageFormat.format;
import static org.apache.log4j.Logger.getLogger;

/**
 * Decrypting inbox for native applications.
 *
 * @author $Author: sasha $
 * @version $Revision: 349 $
 */
public class DecryptingInbox extends NativeAppInbox {
  private static final Logger LOG = getLogger(DecryptingInbox.class.getName());

  private final EntryNameResolver entryNameResolver = new EntryNameResolver();

  /**
   * @param directory the directory of the inbox
   * @param sedexId   sedexId
   * @param types     msgTypes to handle
   * @throws ConfigurationException Config problems
   */
  public DecryptingInbox(final File directory, final String sedexId, final Collection<MessageType> types)
          throws ConfigurationException {
    super(directory, sedexId, types);
  }

  @Override
  public void extract(final MessageHandlerContext context, final Message message) throws IOException {
    final ProtocolService protocolService = context.getProtocolService();
    final ClientConfiguration clientConfig = context.getClientConfiguration();

    LOG.debug("unpacking and decrypting message with message_ID=" + message.getMessageId());

    final Decryptor decryptor = clientConfig.getDecryptor();

    try {
      final String filename = entryNameResolver.getFileEntryNameFromContainer(message.getDataFile());

      // protocol
      protocolService.logReceiving(filename, message);

      final File decryptedContainer = decryptor.decrypt(message.getDataFile());

      message.addFile(decryptedContainer);

      LOG.debug("extracted file: " + decryptedContainer.getAbsolutePath());

    } catch (CryptographyException e) {
      LOG.error(format("the container file {0} is cannot be properly decrypted and will be moved to the 'corrupted' "
                      + "directory",
              message.getDataFile().getAbsolutePath()), e);
    } catch (InvalidContainerException e) {
      LOG.error(format("the container file {0} has invalid structure and will be moved to the 'corrupted' directory",
              message.getDataFile().getAbsolutePath()), e);
    } catch (IOException e) {
      LOG.error(format("the file {0} is cannot be properly is not a ZIP file and will be moved to the "
              + "'corrupted' directory", message.getDataFile().getAbsolutePath()), e);
    }
  }

  @Override
  public void receive(final MessageHandlerContext context, final Message message)
          throws IOException {
    final ClientConfiguration clientConfig = context.getClientConfiguration();
    final File corruptedDir = new File(new File(clientConfig.getWorkingDir()), ClientCommons.CORRUPTED_DIR);

    if (message.getFiles().isEmpty()) {
      // move to the corrupted???
      copy(message, corruptedDir);

      // if everything is ok
      LOG.info(MessageFormat.format("file {0} received, but put into the corrupted directory {1}",
              message.getDataFile().getName(), corruptedDir));
    } else {
      final File targetDirectory = new File(getDirectory(), message.getMessageId());

      targetDirectory.mkdir();

      // move the file in the message to the inbox
      for (File incomingFile : message.getFiles()) {
        // pack them out directly into the inbox
        ZipUtils.decompress(incomingFile, targetDirectory);

        // if everything is ok
        LOG.info(MessageFormat.format("file {0} received and decomprtessed into {1}",
                incomingFile.getName(),
                targetDirectory));
      }

    }

  }

  private void copy(final Message message, final File targetDir) throws IOException {
    FileUtils.copyIntoDirectory(message.getEnvelopeFile(), targetDir);
    FileUtils.copyIntoDirectory(message.getDataFile(), targetDir);
  }

}
