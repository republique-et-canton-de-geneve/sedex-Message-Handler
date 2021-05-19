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

package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.config.Mailbox;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

/**
 * A <code>ReceiptsFolder</code> instance represents a folder for Sedex receipts
 * managed by an external application.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class ReceiptsFolder extends Mailbox {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReceiptsFolder.class.getName());

  private String sedexId;

  private List<MessageType> messageTypes = new ArrayList<MessageType>();

  /**
   * Creates a new mapping for a receipts folder.
   *
   * @param directory
   *          absolute path
   * @param sedexId
   *          the sedex ID of the participant expecting Sedex receipts in this
   *          folder
   * @param messageTypes
   *          the types of the messages whose receipt are expected in this
   *          folder
   */
  public ReceiptsFolder(File directory, String sedexId, List<MessageType> messageTypes) throws ConfigurationException {
    super(directory);
    this.sedexId = sedexId;
    this.messageTypes = messageTypes;

    LOG.info("Created ReceiptsFolder: SedexId: " + StringUtils.defaultIfEmpty(sedexId, ClientCommons.NOT_SPECIFIED)
            + ", Types: {" + StringUtils.defaultIfEmpty(MessageType.collectionToString(messageTypes),
            ClientCommons.NOT_SPECIFIED) + "}, Path: " + directory.getAbsolutePath());
  }

  /**
   * @return Returns the sedexId.
   */
  public String getSedexId() {
    return sedexId;
  }

  /**
   * Returns the configured message type or <code>null</code> if none.
   *
   * @return
   */
  public List<MessageType> getMessageTypes() {
    return Collections.unmodifiableList(messageTypes);
  }

  /**
   * Returns <code>true</code>, if this folder is configured for the provided message
   * type.
   *
   * @param type
   *          the message type
   * @return if this folder supports the given message type; if the message type
   *         provided to this method is <code>null</code>, this method will
   *         return <code>true</code>, only if the folder's list of configured
   *         message types is empty
   */
  public boolean isConfiguredFor(MessageType type) {
    if (null == type) {
      if (messageTypes.isEmpty()) {
        // only if
        return true;
      }
    }

    // search
    for (MessageType messageType : messageTypes) {
      if (type.getType() == messageType.getType()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String toString() {
    return MessageFormat.format("name: {0}; types of the checked messages: {1}; sedex ID of the participant: {2};",
//        StringUtils.defaultIfEmpty(getDirectory(), ClientCommons.NOT_SPECIFIED),
        getDirectory(),
        StringUtils.defaultIfEmpty(MessageType.collectionToString(messageTypes), ClientCommons.NOT_SPECIFIED),
        StringUtils.defaultIfEmpty(getSedexId(), ClientCommons.NOT_SPECIFIED));
  }

}
