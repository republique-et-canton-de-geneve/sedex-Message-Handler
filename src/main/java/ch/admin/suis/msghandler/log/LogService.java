/*
 * $Id$
 *
 * Copyright (C) 2006 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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

package ch.admin.suis.msghandler.log;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Interface for tracing the status of the sent files.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public interface LogService {

  /**
   * Sets the status of the given file to SENDING. If the given file for this
   * recipient has been already sent, then this method returns
   * <code>false</code>.
   *
   * @param source the source of the message (MessageHandler or transparently dispatched)
   *
   * @param participantId
   *          the ID of the participant application to which the message is sent
   * @param filename
   *          the name of the file being sent
   *
   * @return <code>true</code>, if the status SENDING has been successfully
   *         set, and <code>false</code> otherwise
   *
   * @throws LogServiceException
   *           if an underlying problem prevents from sending the file
   */
  boolean setSending(Mode source, String participantId, String filename)
      throws LogServiceException;

  /**
   * Sets the status of the given file to SENDING. If the given file for this recipient has been already sent, then this
   * method returns
   * <code>false</code>.
   *
   * @param source the source of the message (MessageHandler or transparently dispatched)
   *
   * @param participantIds list of the ID of the participant application to which the message is sent
   * @param filename the name of the file being sent
   *
   * @return <code>true</code>, if the status SENDING has been successfully set, and <code>false</code> otherwise
   *
   * @throws LogServiceException if an underlying problem prevents from sending the file
   */
  boolean setSending(Mode source, List<String> participantId, String filename)
          throws LogServiceException;

  /**
   * Sets the status of the given file to SENT.
   * @param source the source of the message (MessageHandler or transparently dispatched)
   *
   * @param participantIds
   *          list of the ID of the participant application to which the message has
   *          been sent
   * @param filename
   *          the name of the sent file
   *
   * @param messageId
   *          the message ID this file has been packed into
   *
   * @throws LogServiceException
   *           if an underlying problem prevents from properly setting the
   *           status
   */
  void setForwarded(Mode source, List<String> participantIds, String filename, String messageId)
      throws LogServiceException;

    /**
   * Sets the status of the given file to SENT.
   * @param source the source of the message (MessageHandler or transparently dispatched)
   *
   * @param participantId
   *          the ID of the participant application to which the message has
   *          been sent
   * @param filename
   *          the name of the sent file
   *
   * @param messageId
   *          the message ID this file has been packed into
   *
   * @throws LogServiceException
   *           if an underlying problem prevents from properly setting the
   *           status
   */
  void setForwarded(Mode source, String participantId, String filename, String messageId)
    throws LogServiceException;

  /**
   * Returns the list of message IDs for the messages that have the status SENT
   * or FORWARDED. All participants are considered.
   *
   * The messages are looked up in the log. The unique occurence of the message
   * IDs is garanteered. If nothing can be found, this method returns an empty
   * list.
   * @throws LogServiceException
   *           if some underlying problem prevents this operation from
   *           completion
   *
   * @return the list of the message IDs (represented as strings) or an empty
   *         list
   */
  List<String> getSentMessages() throws LogServiceException;

  /**
   * Returns the list of message IDs for the messages that have the given
   * status. All participants are considered.
   *
   * The messages are looked up in the log. The unique occurence of the message
   * IDs is garanteered. If nothing can be found, this method returns an empty
   * list.
   *
   * @throws LogServiceException
   *           if some underlying problem prevents this operation from
   *           completion
   *
   * @return the list of the message IDs (represented as strings) or an empty
   *         list
   */
  List<String> getMessages(LogStatus status) throws LogServiceException;

  /**
   * Logs the status change for the given message. This method looks in the log
   * for the files belonging to this message ID and sets the status change date
   * and the status itself for them.
   *
   * @param messageId
   *
   * @param changeDate
   *          the status change date
   *
   * @param status
   *          the new message status
   *
   * @throws LogServiceException
   *           if some underlying problem prevents this operation from
   *           completion
   */
  void setStatusChange(String messageId, Date changeDate, LogStatus status)
      throws LogServiceException;

  /**
   * Returns the names of the files belonging to the specified message. If
   * nothing can be found, this message returns an empty list.
   *
   * @param messageId
   *          the message ID
   *
   * @return the list of filenames (represented as strings) or an empty list
   *
   * @throws LogServiceException
   *           if some underlying problem prevents this operation from
   *           completion
   */
  List<String> getFiles(String messageId) throws LogServiceException;

  /**
   * Returns all entries in the DB.
   *
   * @return
   * @throws LogServiceException
   */
  List<DBLogEntry> getAllEntries() throws LogServiceException;

  /**
   * Returns the time when the message with the provided ID was sent or
   * <code>null</code> if the message cannot be found or was not sent.
   *
   * @param messageId
   *          the ID of the message
   *
   * @return the sent date or <code>null</code> if not found or not sent
   *
   * @throws LogServiceException
   *           if the internal log DB cannot be accessed for whatever reason
   */
  Date getSentDate(String messageId) throws LogServiceException;

  /**
   * Removes the log records that are already aged. If some error occurs these
   * method will just return <code>false</code>
   *
   * @return the if some records were removed; otherwise this method returns
   *         <code>false</code>
   *
   */
  Collection<String> removeAged();

  /**
   * Returns <code>true</code>, if the message with the provided ID was prepared
   * by some external application.
   *
   * @param messageId
   * @return
   *
   * @throws LogServiceException if the message cannot be found or a DB error occured
   */
  boolean isTransparent(String messageId) throws LogServiceException;

}
