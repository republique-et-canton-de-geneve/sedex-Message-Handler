/*
 * $Id: Message.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.sender.SenderSession;
import ch.admin.suis.msghandler.util.ISO8601Utils;
import ch.admin.suis.msghandler.xml.EnvelopeTypeParent;
import ch.admin.suis.msghandler.xml.v1.V1Envelope;
import ch.admin.suis.msghandler.xml.v2.V2Envelope;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The <code>Message</code> class describes the content and attributes of the
 * incoming or outgoing ZIP files together with their xml or envelopes.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class Message {

	private String messageId;
	private String eventDate;
	/**
	 * when this message was created
	 */
	private String messageDate;
	private final List<String> recipientIds = new ArrayList<>();
	private String senderId;
	private MessageType messageType;
	private String messageClass;
	private ObjectVersion version;

	/**
	 * collection of files to be sent or received
	 */
	private List<File> files = new ArrayList<>();
	/**
	 * pointer to the data file of the message
	 */
	private File dataFile;
	/**
	 * pointer to the envelope file of the message
	 */
	private File envelopeFile;
	/**
	 * Adds a new file description to the internal list of files. A file cannot be
	 * added twice.
	 *
	 * @param file file to be included into this message; cannot be <code>null</code>
	 * @throws NullPointerException if the file parameter is <code>null</code>
	 */
	public void addFile(File file) {
		Validate.notNull(file, "cannot add null as the message file");
		this.files.add(file);
	}

	/**
	 * Adds the files.
	 *
	 * @param files files to add to a message
	 */
	public void addFiles(Collection<File> files) {
		this.files.addAll(files);
	}

	/**
	 * Returns the data file associated with this message.
	 *
	 * @return the list of files linked to this message.
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
	 * @throws IllegalArgumentException if the parameter value is not in messageDate
	 * @throws NullPointerException     if the parameter is <code>null</code>
	 */
	public void setEventDate(String eventTime) {
		Validate.isTrue(ISO8601Utils.isISO8601Date(eventTime), "The event date must be ISO 8601 compliant");
		this.eventDate = eventTime;
	}

	/**
	 * @return Returns the messageId.
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * @param messageId The messageId to set.
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
	 * @param messageType The messageType to set.
	 */
	public void setMessageType(Integer messageType) {
		this.messageType = messageType == null ? null
				: new MessageType(messageType);
	}

	/**
	 * Sets the message type.
	 *
	 * @param messageType The type of message.
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
	 * @param messageClass The messageClass to set.
	 */
	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}

	/**
	 * Gets a list of all recipients. If no recipient the list will be empty.
	 *
	 * @return Returns the recipientIds.
	 */
	public List<String> getRecipientIds() {
		return recipientIds;
	}

	/**
	 * @param recipientId The recipientId to add.
	 */
	public void addRecipientId(String recipientId) {
		this.recipientIds.add(recipientId);
	}

	/**
	 * @return Returns the senderId.
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId The senderId to set.
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
	 * @throws IllegalArgumentException if the parameter value is not in messageDate
	 * @throws NullPointerException     if the parameter is <code>null</code>
	 */
	public void setMessageDate(String createTime) {
		Validate.isTrue(ISO8601Utils.isISO8601Date(createTime), "The message date must be ISO 8601 compliant");
		this.messageDate = createTime;
	}

	/**
	 * @return Returns the dataFile.
	 */
	public File getDataFile() {
		return dataFile;
	}

	/**
	 * @param dataFile The dataFile to set.
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
	 * Returns the version of the message.
	 * @return
	 */
	public ObjectVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of the message.
	 * @param version
	 */
	public void setVersion(ObjectVersion version) {
		this.version = version;
	}

	/**
	 * @param envelopeFile The envelopeFile to set.
	 */
	public void setEnvelopeFile(File envelopeFile) {
		this.envelopeFile = envelopeFile;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Message) {
			Message other = (Message) obj;
			return new EqualsBuilder().append(this.messageId, other.messageId)
					.isEquals();
		} else {
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
	 * @throws IOException  if an error occured while writing the envelope
	 * @throws SAXException if the created message is invalid and cannot be validated against a known Sedex schema (the
	 *                      envelope is not written in this case)
	 */
	public void writeEnvelope(Writer writer) throws IOException, SAXException {
		// Note : we are not supposed to send any V1 envelope, we only emit V2.
		String xmlString = SenderSession.msgGen.generate(this);
		writer.write(xmlString);
	}

	/**
	 * Temporarily serializes the message content to a string in XML format and validates it.
	 *
	 * @throws SAXException if the validation failed
	 * @throws IOException  if something went wrong while serializing the message
	 */
	void validate() throws SAXException, IOException {
		SenderSession.msgGen.generate(this);
	}

	public String getRecipientsAsString() {
		return StringUtils.join(recipientIds, ", ");
	}

	/**
	 * Creates a message from this reader.
	 *
	 * @param inputStream Flow of data representing a Message.
	 * @return Message a built message.
	 * @throws JAXBException if an error occures while parsing XML
	 */
	public static Message createFrom(InputStream inputStream) throws JAXBException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(inputStream, baos);
		byte[] bytes = baos.toByteArray();

		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(V2Envelope.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			V2Envelope envelope = (V2Envelope) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(bytes));
			return EnvelopeTypeParent.toMessage(envelope);
		} catch (UnmarshalException e){
			JAXBContext jaxbContext = JAXBContext.newInstance(V1Envelope.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			V1Envelope envelope = (V1Envelope) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(bytes));
			return EnvelopeTypeParent.toMessage(envelope);
		}
	}

}
