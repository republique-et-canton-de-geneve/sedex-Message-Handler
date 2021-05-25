/*
 * $Id: Outbox.java 327 2014-01-27 13:07:13Z blaser $
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
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.naming.NamingService;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a configured outbox.
 * It's a hack. This class can be a "NativeOutbox" or a "TransparentOutbox".
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class Outbox extends Mailbox {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Outbox.class.getName());

  private List<SigningOutbox> signingOutboxes = new LinkedList<>();

  private final MessageType type;

  private final String sedexId;

  private final NamingService participantIdResolver;

  /**
   * Number of seconds the system has to wait before sending a file. Native mode only
   */
  public static long secondsBeforeSending = 0;

  /**
   * Use this constructor to create a NATIVE Outbox!
   * Creates a new outbox.
   *
   * @param directory the name of the outbox
   * @param sedexId   the messages from this outbox will receive this ID
   * @param type      the message type.
   */
  public Outbox(File directory, String sedexId, MessageType type, NamingService participantIdResolver)
          throws ConfigurationException {

    super(directory);

    this.sedexId = sedexId;
    this.type = type;
    this.participantIdResolver = participantIdResolver;
    Validate.notNull(participantIdResolver, "ParticipantIdResolver is required for a native Outbox.");

    LOG.info("Created NativeOutbox: SedexId: " + sedexId + ", Type: " + type + ", Path: "
            + directory.getAbsolutePath());
  }

  /**
   * Use this constructor to create a TRANSPARENT Outbox!
   * Creates a new outbox.
   *
   * @param directory the name of the outbox
   */
  public Outbox(File directory) throws ConfigurationException {
    super(directory);

    this.sedexId = null;
    this.type = null;
    this.participantIdResolver = null;

    LOG.info("Created TransparentOutbox: Path: " + directory.getAbsolutePath());
  }

  /**
   * Returns the (optional) message type associated with this outbox. If there is no associated message type, this
   * method returns
   * <code>null</code>.
   *
   * @return MessageType msg type.
   */
  public MessageType getType() {
    return type;
  }

  /**
   * Returns the (optional) sedex ID that will be assigned to the messages originating from this outbox. If none is
   * configured, this method returns
   * <code>null</code>.
   *
   * @return Returns the sedexId.
   */
  public String getSedexId() {
    return sedexId;
  }

  /**
   * Gets all signining Outboxes which belongs to this Outbox. <p /> This is part of the new MH 3.0 functionality for
   * signing PDFs.
   *
   * @return The list of signing outboxes.
   */
  public List<SigningOutbox> getSigningOutboxes() {
    return signingOutboxes;
  }

  /**
   * @param signingOutbox The new signing outbox.
   */
  public void addSigningOutbox(SigningOutbox signingOutbox) {
    signingOutboxes.add(signingOutbox);
  }

  /**
   * Only for native Applications!
   * Returns the service to resolve the participant whom the messages from this outbox
   * will be sent to.
   *
   * @return NamingService
   */
  public NamingService getParticipantIdResolver() {
    return participantIdResolver;
  }

  @Override
  public String toString() {
    return MessageFormat.format("name: {0}; type of the outgoing messages: {1}; sedex ID of the sender: {2};",
            //        StringUtils.defaultIfEmpty(getDirectory(), ClientCommons.NOT_SPECIFIED),
            getDirectory(),
            null == type ? ClientCommons.NOT_SPECIFIED : type.toString(),
            StringUtils.defaultIfEmpty(getSedexId(), ClientCommons.NOT_SPECIFIED));
  }
}