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
package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.naming.NamingService;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ZipUtils;
import ch.glue.fileencryptor.CryptographyException;
import ch.glue.fileencryptor.Decryptor;
import ch.glue.fileencryptor.EntryNameResolver;
import ch.glue.fileencryptor.InvalidContainerException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.apache.log4j.Logger.getLogger;

/**
 * Decrypting inbox for native applications.
 *
 * @author $Author$
 * @version $Revision$
 */
public class DecryptingInbox extends NativeAppInbox
{
  private static final Logger LOG = getLogger(DecryptingInbox.class.getName());

  private final EntryNameResolver entryNameResolver = new EntryNameResolver();

  private final Decryptor decryptor;

  private NamingService renamingScript;

  /**
   * @param directory
   *     the directory of the inbox
   * @param sedexId
   *     sedexId
   * @param types
   *     msgTypes to handle
   * @param decryptor configured decryptor
   * @throws ConfigurationException
   */
  public DecryptingInbox(final File directory, final String sedexId, final Collection<MessageType> types,
      final Decryptor decryptor)
      throws ConfigurationException
  {
    super(directory, sedexId, types);

    this.decryptor = decryptor;
  }

  /**
   * Sets the script that should rename the target folder.
   *
   * @param renamingScript
   */
  public void setRenamingScript(final NamingService renamingScript)
  {
    this.renamingScript = renamingScript;
  }

  @Override
  public void extract(final MessageHandlerContext context, final Message message) throws IOException
  {
    final ProtocolService protocolService = context.getProtocolService();

    LOG.debug("unpacking and decrypting message with message_ID=" + message.getMessageId());

    try
    {
      final String filename = entryNameResolver.getFileEntryNameFromContainer(message.getDataFile());

      // protocol
      protocolService.logReceiving(filename, message);

      final File decryptedContainer = decryptor.decrypt(message.getDataFile());

      message.addFile(decryptedContainer);

      LOG.debug("extracted file: " + decryptedContainer.getAbsolutePath());

    } catch (CryptographyException e)
    {
      LOG.error(format("the container file {0} cannot be properly decrypted and will be moved to the 'corrupted' "
              + "directory",
          message.getDataFile().getAbsolutePath()), e);
    } catch (InvalidContainerException e)
    {
      LOG.error(format("the container file {0} has invalid structure and will be moved to the 'corrupted' directory",
          message.getDataFile().getAbsolutePath()), e);
    } catch (IOException e)
    {
      LOG.error(format("the file {0} is not a ZIP file and will be moved to the 'corrupted' directory",
          message.getDataFile().getAbsolutePath()), e);
    }
  }

  @Override
  public void receive(final MessageHandlerContext context, final Message message)
      throws IOException
  {
    final ClientConfiguration clientConfig = context.getClientConfiguration();
    final File corruptedDir = new File(new File(clientConfig.getWorkingDir()), ClientCommons.CORRUPTED_DIR);

    if (message.getFiles().isEmpty())
    {
      // move to the corrupted???
      copy(message, corruptedDir);

      // if everything is ok
      LOG.info(MessageFormat.format("file {0} received, but put into the corrupted directory {1}",
          message.getDataFile().getName(), corruptedDir));
    }
    else
    {
      // move the file in the message to the inbox
      for (File incomingFile : message.getFiles())
      {
        final File targetDirectory = getTargetDirectory(message, incomingFile);

        // pack them out directly into the inbox
        ZipUtils.decompress(incomingFile, targetDirectory);

        // if everything is ok
        LOG.info(MessageFormat.format("file {0} received and decompressed into {1}",
            incomingFile.getName(),
            targetDirectory));
      }
    }
  }

  private File getTargetDirectory(final Message message, File incomingFile) throws IOException
  {
    // change to another directory name if necessary
    if (null != renamingScript)
    {
      // read the receipt in JSON
      final Map<String, Object> communicationConfirmationMap = new CommunicationConfirmation(incomingFile).extract();

      if (null != communicationConfirmationMap)
      {
        String newTargetName = renamingScript.resolve(communicationConfirmationMap);

        if (StringUtils.isNotEmpty(newTargetName))
        {
          return ensurePath(new File(FileUtils.getFilename(getDirectory(), newTargetName)));
        }
      }
      else
      {
        LOG.warn("cannot read communication-confirmation.json from " + incomingFile.getAbsolutePath());
      }
    }

    return ensurePath(new File(getDirectory(), message.getMessageId()));
  }

  private static File ensurePath(File target)
  {
    target.mkdir();
    return target;
  }

  private void copy(final Message message, final File targetDir) throws IOException
  {
    FileUtils.copyIntoDirectory(message.getEnvelopeFile(), targetDir);
    FileUtils.copyIntoDirectory(message.getDataFile(), targetDir);
  }

}
