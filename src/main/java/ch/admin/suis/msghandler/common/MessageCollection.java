/*
 * $Id: MessageCollection.java 340 2015-08-16 14:51:19Z sasha $
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

import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a collection of Sedex messages
 * residing in a specified directory.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public class MessageCollection {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(MessageCollection.class.getName());

	/**
	 * the pattern to extract the suffix from the envelope's file name
	 */
	private static final Pattern SUFFIX_PATTERN = Pattern.compile("envl_(\\S+)\\.xml");

	private File messageDir;

	/**
	 * Creates a {@link MessageCollection} for the specified
	 * directory.
	 *
	 * @param messageDir Path
	 */
	public MessageCollection(File messageDir) {
		this.messageDir = messageDir;
	}

	/**
	 * Returns the messages.
	 *
	 * @return List of message in the collection
	 */
	public Collection<Message> get() {

		LOG.debug("Scanning directory for messages. Path: " + messageDir.getAbsolutePath());

		List<File> envelopeFiles = catchAllEnvelopeFiles();
		List<File> dataFiles = catchAllDataFiles();
		if (envelopeFiles.isEmpty() || dataFiles.isEmpty()) {
			LOG.debug(
					MessageFormat.format(
							"No {0} files available. Nothing todo...",
							envelopeFiles.isEmpty() ? "envelope" : "data"
					));
			return Collections.emptyList();
		}

		final ArrayList<Message> result = new ArrayList<>();

		for (File envelope : envelopeFiles) {

			Message message = readMessageFile(envelope);
			if (message != null) {
				message.setEnvelopeFile(envelope);

				final String suffix = extractSuffixFromName(envelope);
				if (StringUtils.isEmpty(suffix)) {
					LOG.error("envelope does not follow the naming convention: " + envelope.getAbsolutePath());
					continue; //go on with the next element in the loop
				}
				result.addAll(iterateOverDataFiles(dataFiles, suffix, message, envelope));
			} else {
				LOG.info("Unable to read message file. " + envelope.getAbsolutePath());
			}


		}

		return result;
	}

	private ArrayList<Message> iterateOverDataFiles(List<File> dataFiles, String suffix, Message message, File envelope) {
		ArrayList<Message> result = new ArrayList<>();
		boolean matchComplete = false; //matchComplete: just for logging

		Iterator<File> dataIterator = dataFiles.iterator();
		while (dataIterator.hasNext()) {
			File dataFile = dataIterator.next();

			//Double if: 1. Check if its start with same name as the envelope file. 2. Check if its really a readable file
			if (dataFile.getName().startsWith("data_" + suffix + ".") && FileFilters.isReadableFile(dataFile)) {
				//Found it. Complete the message object
				message.setDataFile(dataFile);
				result.add(message);

				matchComplete = true;
				//Remove the element from the underlying list. Optimization reasons. Abort the loop.
				dataIterator.remove();
				break; //leave the dataIterator loop
			}
		}

		//Just logging...
		if (!matchComplete) {
			LOG.error("cannot find or cannot read the data files for the envelope " + envelope.getAbsolutePath());
		} else {
			LOG.info(MessageFormat.format("reading the data file {0} for the message ID {1}",
					message.getDataFile().getAbsolutePath(), message.getMessageId()));
		}
		return result;
	}

	/**
	 * Reads the envelope file and generates a message object.
	 *
	 * @param envelope File representing an envelope.
	 * @return the Message. Or null if an error occurred.
	 */
	private Message readMessageFile(File envelope) {
		Message message = null;
		try (InputStream reader = new FileInputStream(envelope)) {
			LOG.debug("Create message from: " + envelope.getAbsolutePath());
			message = Message.createFrom(reader);
		} catch (IOException e) {
			LOG.error("cannot read the envelope file " + envelope.getAbsolutePath() + "; file skipped ", e);
		} catch (JAXBException e) {
			LOG.error("cannot parse the envelope file " + envelope.getAbsolutePath() + "; file skipped ", e);
		}

		return message;
	}

	/**
	 * Returns all Envelope files. The files are readable and not locked by an other process.
	 *
	 * @return All envelopes
	 */
	private List<File> catchAllEnvelopeFiles() {
		LOG.debug("Get all envelop files from: " + messageDir.getAbsolutePath() + ". This may take long time");
		DirectoryStream<Path> envelopeFiles = FileUtils.listFiles(messageDir, FileFilters.ENVELOPE_FILTER_PATH);

		if (envelopeFiles == null) {
			LOG.error("an I/O error occured while reading the Sedex envelopes from the directory " + messageDir.
					getAbsolutePath() + "; check the message handler configuration to see whether the specified directory "
					+ "actually exists");

			return Collections.emptyList();
		}

		return directoryStreamToListOfFiles(envelopeFiles);
	}

	/**
	 * Returns all Data files. There is no check about if the data file is locked or not.
	 *
	 * @return All data files
	 */
	private List<File> catchAllDataFiles() {
		LOG.debug("Get all data files from: " + messageDir.getAbsolutePath() + ". This may take long time");
		DirectoryStream<Path> dataFiles= FileUtils.listFiles(messageDir, FileFilters.DATA_FILES_FILTER_PATH);

		if (dataFiles == null) {
			LOG.error("an I/O error occured while reading the Sedex data files from the directory " + messageDir.getAbsolutePath() +
					"; check the message handler configuration to see whether the specified directory " +
					"actually exists");
			return Collections.emptyList();
		}

		return directoryStreamToListOfFiles(dataFiles);
	}

	private List<File> directoryStreamToListOfFiles(DirectoryStream<Path> stream){
		List<File> envFiles = new ArrayList<>();
		for (Path path : stream){
			envFiles.add(path.toFile());
		}
		try{
			stream.close();
		} catch (IOException e){
			LOG.error("Unable to close directory stream. " + e);
		}
		return envFiles;
	}

	/**
	 * The middle name of the file. envl_{suffix}.xml<br/>
	 * This method is required to find the corresponding data file. data_{suffix}.* <p />
	 * <p>
	 * Example: The input: evnl_11-22-33.xml, will return 11-22-33.
	 *
	 * @param envelopeFile file from the envelope
	 * @return null if wrong format. Otherwise the middle part -suffix- from the envelope filename. Such as envl_{suffix}.xml
	 */
	private String extractSuffixFromName(File envelopeFile) {
		final Matcher matcher = SUFFIX_PATTERN.matcher(envelopeFile.getName()); //envelope.getName()
		if (!matcher.find()) {
			return null;
		}

        /*
		 * The middle name of the message. envl_{suffix}.xml and data_{suffix}.*
         */
		return matcher.group(1);
	}
}
