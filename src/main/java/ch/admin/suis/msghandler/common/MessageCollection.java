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

import ch.admin.suis.msghandler.util.FileFilters;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a collection of Sedex messages
 * residing in a specified directory.
 *
 * @author      Alexander Nikiforov
 * @author      $Author$
 * @version     $Revision$
 */
public class MessageCollection {
  /** logger */
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
   * @param messageDir
   */
  public MessageCollection(File messageDir) {
    this.messageDir = messageDir;
  }

  /**
   * Returns the messages.
   *
   * @return
   */
  public Collection<Message> get() {

    LOG.debug("Scanning directory for messages. Path: " + messageDir.getAbsolutePath());

    List<File> envelopeFiles = catchAllEnvelopeFiles();
    if(envelopeFiles.isEmpty()){
      LOG.debug("No envelop files available. Nothing todo...");
      return Collections.emptyList();
    }

    List<File> dataFiles = catchAllDataFiles();
    if(dataFiles.isEmpty()){
      LOG.debug("Envelope file available. But not yet a datafile. Nothing todo...");
      return Collections.emptyList();
    }

    final ArrayList<Message> result = new ArrayList<Message>();

    for (File envelope : envelopeFiles) {

      Message message = readMessageFile(envelope);
      if(message == null){
        LOG.info("Unable to read message file. " + envelope.getAbsolutePath());
        continue; //go on with the next element in the loop
      }

      message.setEnvelopeFile(envelope);

      final String suffix = extractSuffixFromName(envelope);
      if(StringUtils.isEmpty(suffix)){
        LOG.error("envelope does not follow the naming convention: " + envelope.getAbsolutePath());
        continue; //go on with the next element in the loop
      }

      boolean matchComplete = false; //matchComplete: just for logging

      /**
       * Use an Iterator instead make a foreach loop over the list. Because we will remove elements from it. It's not
       * allowed todo that inside a foreach loop.
       */
      Iterator<File> dataIterator = dataFiles.iterator();
      while(dataIterator.hasNext())
      {
        File dataFile = dataIterator.next();

        //Double if: 1. Check if its start with same name as the envelope file. 2. Check if its really a readable file
        if (dataFile.getName().startsWith("data_" + suffix + "."))
        {
          if (FileFilters.isReadableFile(dataFile))
          {
            //Found it. Complete the message object
            message.setDataFile(dataFile);
            result.add(message);

            matchComplete = true;
            //Remove the element from the underlying list. Optimization reasons. Abort the loop.
            dataIterator.remove();
            break; //leave the dataIterator loop
          }
        }
      }

      //Just logging...
      if (!matchComplete)
      {
        LOG.error("cannot find or cannot read the data files for the envelope " + envelope.getAbsolutePath());
      } else
      {
        LOG.info(MessageFormat.format("reading the data file {0} for the message ID {1}",
            message.getDataFile().getAbsolutePath(), message.getMessageId()));
      }
    }

    return result;
  }

  /**
   * Reads the envelope file and generates a message object.
   *
   * @param envelope
   * @return the Message. Or null if an error occurred.
   */
  private Message readMessageFile(File envelope)
  {
    Message message = null;
    InputStream reader = null;
    try
    {
      reader = new FileInputStream(envelope);
      LOG.debug("Create message from: " + envelope.getAbsolutePath());
      message = Message.createFrom(reader);
    } catch (IOException e)
    {
      // TODO what to do if the receiver cannot read the envelope?
      LOG.error("cannot read the envelope file " + envelope.getAbsolutePath() + "; file skipped ", e);
    } catch (SAXException e)
    {
      // TODO what to do if the receiver cannot parse the envelope?
      LOG.error("cannot parse the envelope file " + envelope.getAbsolutePath() + "; file skipped ", e);
    } finally
    {
      if (null != reader)
      {
        try
        {
          reader.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }

    return message;
  }

  /**
   * Returns all Envelope files. The files are readable and not locked by an other process.
   * @return
   */
  private List<File> catchAllEnvelopeFiles()
  {
    LOG.debug("Get all envelop files from: " + messageDir.getAbsolutePath() + ". This may take long time");
    File[] envelopeFiles = messageDir.listFiles(FileFilters.ENVELOPE_FILTER);

    if (null == envelopeFiles)
    {
      LOG.error("an I/O error occured while reading the Sedex envelopes from the directory " + messageDir.
          getAbsolutePath() + "; check the message handler configuration to see whether the specified directory "
          + "actually exists");

      return Collections.emptyList();
    }

    return Arrays.asList(envelopeFiles);
  }

  /**
   * Returns all Data files. There is no check about if the data file is locked or not.
   * @return
   */
  private List<File> catchAllDataFiles()
  {
    LOG.debug("Get all data files from: " + messageDir.getAbsolutePath() + ". This may take long time");
    File[] dataFiles = messageDir.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.startsWith("data_");
      }
    });

    if (dataFiles == null)
    {
      LOG.error("an I/O error occured while reading the Sedex data files from the directory " + messageDir.getAbsolutePath() +
          "; check the message handler configuration to see whether the specified directory " +
          "actually exists");
      return Collections.emptyList();
    }

    LOG.info("Number of datafiles in directory: " + dataFiles.length);

    return new ArrayList<File>(Arrays.asList(dataFiles));
  }

  /**
   * The middle name of the file. envl_{suffix}.xml<br/>
   * This method is required to find the corresponding data file. data_{suffix}.* <p />
   *
   * Example: The input: evnl_11-22-33.xml, will return 11-22-33.
   *
   * @param envelopeFile file from the envelope
   * @return null if wrong format. Otherwise the middle part -suffix- from the envelope filename. Such as envl_{suffix}.xml
   */
  private String extractSuffixFromName(File envelopeFile)
  {
    final Matcher matcher = SUFFIX_PATTERN.matcher(envelopeFile.getName()); //envelope.getName()
    if (!matcher.find())
    {
      return null;
    }

    /**
     * The middle name of the message. envl_{suffix}.xml and data_{suffix}.*
     */
    return matcher.group(1);
  }
}
