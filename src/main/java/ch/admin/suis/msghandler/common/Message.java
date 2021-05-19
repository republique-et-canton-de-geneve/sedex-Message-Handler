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

import ch.admin.suis.msghandler.util.ISO8601Utils;
import ch.admin.suis.msghandler.util.MessageXmlGenerator;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSetBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * The <code>Message</code> class describes the content and attributes of the
 * incoming or outgoing ZIP files together with their receipts or envelopes.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class Message {

  private static final Logger LOG = Logger.getLogger(Message.class.getName());

  private static final RuleSetBase ENVELOPE_RULES = new RuleSetBase() {

    @Override
    public void addRuleInstances(Digester digester) {
      digester.addCallMethod("envelope/messageId", "setMessageId", 0);
      digester.addCallMethod("envelope/messageType", "setMessageType", 0,
          new Class[] { Integer.class });
      digester.addCallMethod("envelope/messageClass", "setMessageClass", 0);
      digester.addCallMethod("envelope/senderId", "setSenderId", 0);
      digester.addCallMethod("envelope/recipientId", "addRecipientId", 0);
      digester.addCallMethod("envelope/messageDate", "setMessageDate", 0);
      digester.addCallMethod("envelope/eventDate", "setEventDate", 0);
    }

  };

  private String messageId;

  private String eventDate;

  /**
   * when this message was created
   */
  private String messageDate;

  private final List<String> recipientIds = new ArrayList<String>();

  private String senderId;

  private MessageType messageType;

  private String messageClass;

  /**
   * collection of files to be sent or received
   */
  private List<File> files = new ArrayList<File>();

  /**
   * pointer to the data file of the message
   */
  private File dataFile;

  /**
   * pointer to the envelope file of the message
   */
  private File envelopeFile;

  public Message() {
  }

  /**
   * Adds a new file description to the internal list of files. A file cannot be
   * added twice.
   *
   * @param file
   *          file to be included into this message; cannot be <code>null</code>
   * @throws NullPointerException
   *           if the file parameter is <code>null</code>
   */
  public void addFile(File file) {
    Validate.notNull(file, "cannot add null as the message file");
    this.files.add(file);
  }

  /**
   * Adds the files.
   *
   * @param files
   */
  public void addFiles(Collection<File> files) {
    this.files.addAll(files);
  }

  /**
   * Returns the data file associated with this message.
   *
   * @return
   */
  public Collection<File> getFiles() {
    return files;
  }

  /**
   * Returns the event time of this message in ISO8601 format.
   *
   * @return Returns the eventDate.
   */
  public String getEventDate() {
    return eventDate;
  }

  /**
   * Sets the event time for this message.
   *
   * @param eventDate
   *          the event time to set; must be in ISO8601 format
   *
   * @throws IllegalArgumentException
   *           if the parameter value is not in messageDate
   * @throws NullPointerException
   *           if the parameter is <code>null</code>
   */
  public void setEventDate(String eventTime) {
    Validate.isTrue(ISO8601Utils.isISO8601Date(eventTime));
    this.eventDate = eventTime;
  }

  /**
   * @return Returns the messageId.
   */
  public String getMessageId() {
    return messageId;
  }

  /**
   * @param messageId
   *          The messageId to set.
   */
  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  /**
   * @return Returns the messageType.
   */
  public MessageType getMessageType() {
    return messageType;
  }

  /**
   * @param messageType
   *          The messageType to set.
   */
  public void setMessageType(Integer messageType) {
    this.messageType = messageType == null ? null
        : new MessageType(messageType);
  }

  /**
   * Sets the message type.
   *
   * @param messageType
   */
  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  /**
   * @return Returns the messageClass.
   */
  public String getMessageClass() {
    return messageClass;
  }

  /**
   * @param messageClass
   *          The messageClass to set.
   */
  public void setMessageClass(String messageClass) {
    this.messageClass = messageClass;
  }

  /**
   * Gets a list of all recipients. If no recipient the list will be empty.
   * @return Returns the recipientIds.
   */
  public List<String> getRecipientIds() {
    return recipientIds;
  }

  /**
   * @param recipientId
   *          The recipientId to add.
   */
  public void addRecipientId(String recipientId) {
//    LOG.info("Add recipient to Message: " + recipientId);
    this.recipientIds.add(recipientId);
  }

  /**
   * @return Returns the senderId.
   */
  public String getSenderId() {
    return senderId;
  }

  /**
   * @param senderId
   *          The senderId to set.
   */
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  /**
   * Returns the creation time of this message in ISO8601 format.
   *
   * @return Returns the messageDate.
   */
  public String getMessageDate() {
    return messageDate;
  }

  /**
   * Sets the creation time of this message.
   *
   * @param messageDate
   *          the create time to set; must be in ISO8601 format
   * @throws IllegalArgumentException
   *           if the parameter value is not in messageDate
   * @throws NullPointerException
   *           if the parameter is <code>null</code>
   */
  public void setMessageDate(String createTime) {
    Validate.isTrue(ISO8601Utils.isISO8601Date(createTime));
    this.messageDate = createTime;
  }

  /**
   * @return Returns the dataFile.
   */
  public File getDataFile() {
    return dataFile;
  }

  /**
   * @param dataFile
   *          The dataFile to set.
   */
  public void setDataFile(File dataFile) {
    this.dataFile = dataFile;
  }

  /**
   * @return Returns the envelopeFile.
   */
  public File getEnvelopeFile() {
    return envelopeFile;
  }

  /**
   * @param envelopeFile
   *          The envelopeFile to set.
   */
  public void setEnvelopeFile(File envelopeFile) {
    this.envelopeFile = envelopeFile;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    else if (obj instanceof Message) {
      Message other = (Message) obj;
      return new EqualsBuilder().append(this.messageId, other.messageId)
          .isEquals();
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.messageId).toHashCode();
  }

  /**
   * Creates a new envelope from the content of this message.
   *
   * @param writer where to write the envelope to
   *
   * @throws IOException if an error occured while writing the envelope
   *
   * @throws SAXException if the created message is invalid and cannot be validated against a known Sedex schema (the
   * envelope is not written in this case)
   */
  public void writeEnvelope(Writer writer) throws IOException, SAXException {
    String xmlString = MessageXmlGenerator.generate(this);
    writer.write(xmlString);
  }

  /**
   * Temporarily serializes the message content to a string in XML format and validates it.
   *
   * @throws SAXException if the validation failed
   * @throws IOException if something went wrong while serializing the message
   */
  void validate() throws SAXException, IOException {
//    final String xmlString = serializeToXml();
    MessageXmlGenerator.generate(this);
  }

  public String getRecipientsAsString(){
    return StringUtils.join(recipientIds, ", ");
  }

  /**
   * Creates a message from this reader.
   * @param inputStream The sedex envelop as an intput stream
   *
   * @return
   *
   * @throws IOException
   *           if an error occures while reading XML
   * @throws SAXException
   *           if an error occures while parsing XML
   */
  public static Message createFrom(InputStream inputStream) throws IOException, SAXException
  {
    final Digester digester = new Digester();
    digester.setNamespaceAware(true);
    digester.addRuleSet(ENVELOPE_RULES);

    XmlParserConfigurator.hardenDigesterAgainstXXE(digester);
    
    final Message message = new Message();
    digester.push(message);

    return (Message) digester.parse(inputStream);
  }

}
