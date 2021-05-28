/*
 * $Id: Inbox.java 340 2015-08-16 14:51:19Z sasha $
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a configured inbox.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public abstract class Inbox extends Mailbox {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Inbox.class.getName());

  /**
   * Defines transparent or native application
   */
  public enum Mode {

    /**
     * Transparent application. Application which is able to speak "Sedex"
     */
    TRANSPARENT,
    /**
     * Native MessageHandler application.
     */
    NATIVE

  }

  private final List<MessageType> types = new ArrayList<>();

  private final String sedexId;

  private final Mode mode;

  public static long incomingMessageLimit = 100;

  /**
   * @param directory the directory of the inbox
   * @param sedexId   sedexId
   * @param types     msgTypes to handle
   * @param mode      transparent application or native MessageHandler application
   * @throws ConfigurationException Config Problems
   */
  public Inbox(File directory, String sedexId, Collection<MessageType> types, Mode mode) throws ConfigurationException {
    super(directory);
    this.mode = mode;
    this.types.addAll(types);
    this.sedexId = sedexId;

    LOG.info("Created Inbox: SedexId: " + StringUtils.defaultIfEmpty(sedexId, ClientCommons.NOT_SPECIFIED)
            + ", Types: {" + StringUtils.defaultIfEmpty(MessageType.collectionToString(types),
            ClientCommons.NOT_SPECIFIED) + "}, Mode: " + mode + ", Path: " + directory.getAbsolutePath());
  }

  /**
   * Receives the given incoming message moving its unpacked files from the temporary folder into the inbox's folder.
   *
   * @param context the current execution context
   * @param message the incoming message
   */
  public abstract void receive(MessageHandlerContext context, Message message) throws IOException;

  /**
   * Receives the given incoming message by unpacking its files into the temporary folder.
   *
   * @param context the current execution context
   * @param message the incoming message
   */
  public abstract void extract(MessageHandlerContext context, Message message) throws IOException;

  /**
   * Returns the list of message types supported by this inbox. Only the messages having the types from this list are
   * allowed to be put into this inbox.
   *
   * @return A list of message types
   */
  public List<MessageType> getMessageTypes() {
    return types;
  }

  /**
   * Returns the sedexId for this inbox. If this ID is not set, this method returns
   * <code>null</code>.
   *
   * @return a list of message types
   */
  public String getSedexId() {
    return sedexId;
  }

  @Override
  public String toString() {

    return MessageFormat.format(
            "name: {0}; transparent: {1}; types of the incoming messages: {2}; sedex ID of the receiver: {3};",
            //        StringUtils.defaultIfEmpty(getDirectory(), ClientCommons.NOT_SPECIFIED),
            getDirectory(),
            mode,
            StringUtils.defaultIfEmpty(MessageType.collectionToString(types), ClientCommons.NOT_SPECIFIED),
            StringUtils.defaultIfEmpty(getSedexId(), ClientCommons.NOT_SPECIFIED));
  }
}
