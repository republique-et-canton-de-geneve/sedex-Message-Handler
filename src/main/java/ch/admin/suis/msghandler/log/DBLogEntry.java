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
 */
package ch.admin.suis.msghandler.log;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents one DB entry in the DBLog.
 *
 * Not for developers: The json serialization will be used for the "Monitor" Get Interface. Do not change any existing
 * attributes! If you do that you need to update the HTTP Interface documentation. Because it would change the result of
 * an HTTP Get request.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 19.07.2012
 */
public class DBLogEntry {

  private String recipientId;

  private String filename;

  private String messageId;

  private String sentDate;

  private String receivedDate;

  private LogStatus state;

  private Mode mode;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public String getReceivedDate() {
    return receivedDate;
  }

  public void setReceivedDate(String receivedDate) {
    this.receivedDate = receivedDate;
  }

  public String getSentDate() {
    return sentDate;
  }

  public void setSentDate(String sentDate) {
    this.sentDate = sentDate;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public LogStatus getState() {
    return state;
  }

  public void setState(LogStatus state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}