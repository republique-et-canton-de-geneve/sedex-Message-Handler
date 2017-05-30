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

import ch.admin.suis.msghandler.config.Inbox;
import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
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

		/* These two methods bellow will be throttled to the maximum processed config. If the config isnt explicitly saying
		how many messages can be processed per cycle, every message will be processed in one go. This tends to be very slow,
		however (50K messages cannot be processed overnight, for example)
		*/
		List<File> envelopeFiles = catchAllEnvelopeFiles();
		List<File> dataFiles = catchAllDataFiles();
		if (envelopeFiles.isEmpty() || dataFiles.isEmpty()) {
			LOG.debug(MessageFormat.format(
					"No {0} files available. Nothing todo...",
					envelopeFiles.isEmpty() ? "envelope" : "data"
			));
			return Collections.emptyList();
		}

		final ArrayList<Message> result = new ArrayList<>();
		LOG.info(
				"Processing " + envelopeFiles.size() + " messages (from " + envelopeFiles.get(0).getName() + " to "
						+ envelopeFiles.get(envelopeFiles.size() - 1).getName() + ")"
		);

		for (File envelope : envelopeFiles) {
			Message message = readMessageFile(envelope);
			if (message != null) {
				message.setEnvelopeFile(envelope);

				final String suffix = extractSuffixFromName(envelope);
				if (StringUtils.isEmpty(suffix)) {
					LOG.error("envelope does not follow the naming convention: " + envelope.getAbsolutePath());
					continue; //go on with the next element in the loop
				}
				try {
					result.add(fetchDataFile(suffix, message, envelope));
				} catch (IllegalStateException e) {
					LOG.info(e);
				}
			} else {
				LOG.info("Unable to read message file. " + envelope.getAbsolutePath());
			}


		}

		return result;
	}

	private Message fetchDataFile(final String suffix, Message message, File envelope) throws IllegalStateException {
		DirectoryStream<Path> fileStream = FileUtils.listFiles(new File(messageDir.getAbsolutePath()), new DirectoryStream.Filter<Path>() {
			public boolean accept(Path pathname) throws IOException {
				return pathname.toFile().getName().startsWith("data_" + suffix + ".") && FileFilters.isReadableFile(pathname);
			}
		});
		List<File> files = directoryStreamToListOfFiles(fileStream);
		if (files.isEmpty()) { // No data files detected
			throw new IllegalStateException("Cannot find the data file for the envelope " + envelope.getAbsolutePath());
		} else if (files.size() > 1){ // Too many data files detected
			throw new IllegalStateException("Several data files have been found corresponding with the suffix " + suffix + ". This is never supposed to happen.");
		} else { // One data files detected, good news
			message.setDataFile(files.get(0));
			LOG.info(MessageFormat.format("reading the data files {0} for the message ID {1}",
					message.getDataFile().getAbsolutePath(), message.getMessageId()));

		}

		return message;
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
		return directoryStreamToListOfFiles(envelopeFiles);
	}

	/**
	 * Returns all Data files. There is no check about if the data file is locked or not.
	 *
	 * @return All data files
	 */
	private List<File> catchAllDataFiles() {
		LOG.debug("Get all data files from: " + messageDir.getAbsolutePath() + ". This may take long time");
		DirectoryStream<Path> dataFiles = FileUtils.listFiles(messageDir, FileFilters.DATA_FILES_FILTER_PATH);
		return directoryStreamToListOfFiles(dataFiles, Long.MAX_VALUE);
	}

	private List<File> directoryStreamToListOfFiles(DirectoryStream<Path> stream) {
		return directoryStreamToListOfFiles(stream, Inbox.incomingMessageLimit);
	}

	private List<File> directoryStreamToListOfFiles(DirectoryStream<Path> stream, long limit){
		int processed = 0;
		List<File> envFiles = new ArrayList<>();
		for (Path path : stream) {
			processed++;
			if (processed > limit) {
				LOG.warn("This job has reached the maximum it could handle. Due to configuration, this job will be throttled. Configuration currently allows " + Inbox.incomingMessageLimit + " messages.");
				break; // This allows to continue without breaking stuff
			}
			envFiles.add(new File(path.toAbsolutePath().toString()));
		}
		try {
			stream.close();
		} catch (IOException e) {
			LOG.error("Unable to close directory stream. " + e);
		}
		return new ArrayList<>(envFiles);
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
