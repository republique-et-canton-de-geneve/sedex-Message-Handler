/*
 * $Id: DbLogServiceJob.java 327 2014-01-27 13:07:13Z blaser $
 *
 * Copyright (C) 2007-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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

import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.Receipt;
import ch.admin.suis.msghandler.util.FileFilters;
import ch.admin.suis.msghandler.util.FileUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <code>DbLogServiceJob</code> removes periodically the aged recods in
 * the log service DB. This task also removes the xml and the data files
 * corresponding to the message IDs of the removed records.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class DbLogServiceJob implements Job {
  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
          .getLogger(DbLogServiceJob.class.getName());

  /**
   * the pattern to extract the suffix from the envelope's file name
   */
  private static final Pattern SUFFIX_PATTERN = Pattern
          .compile("envl_(\\S+)\\.xml");

  /*
   * (non-Javadoc)
   *
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.debug("log table cleanup started");

    // get the objects that are necessary for the sender
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    MessageHandlerContext clientState = (MessageHandlerContext) dataMap
            .get(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM);

    // ************ remove the old xml

    // the Sedex adapter's receipt directory
    File receiptsDir = new File(clientState.getClientConfiguration()
            .getSedexAdapterConfiguration().getReceiptDir());

    // loop over the files in the xml directory
    // check for the files over there
    DirectoryStream<Path> files = FileUtils.listFiles(receiptsDir, FileFilters.XML_FILTER_PATH);

    Collection<String> messageIds = clientState.getLogService().removeAged();

    int count = 0;
    // for each receipt found
    for (Path path : files) {
      count += pipeFilesToReceipt(path.toFile(), messageIds);
    }
    try{
      files.close();
    } catch (IOException e){
      LOG.error("Unable to close stream directory " + e);
    }

    count += cleanDataFiles(clientState, messageIds);

    LOG.info("removed " + count
            + " aged receipts from the receipt directory of the Sedex adapter");
  }

  private int pipeFilesToReceipt(File file, Collection<String> messageIds) {
    int count = 0;
    try (InputStream reader = new FileInputStream(file)) {
      Receipt receipt = Receipt.createFrom(reader);
      if (messageIds.contains(receipt.getMessageId())) {
        // remove this file
        if (file.delete()) {
          count++;
          LOG.info("the receipt removed: " + file.getAbsolutePath());
        } else {
          LOG.error("cannot remove the receipt: " + file.getAbsolutePath());
        }
      }
    } catch (FileNotFoundException e) {
      LOG.error("cannot find the file " + file.getAbsolutePath()
              + "; is it already removed?", e);
    } catch (IOException e) {
      LOG.error("cannot read the file " + file.getAbsolutePath(), e);
    } catch (JAXBException e) {
      LOG.error("cannot parse the file " + file.getAbsolutePath(), e);
    }
    return count;
  }

  /**
   * Deletes the given files
   *
   * @param clientState The message handler context
   * @param messageIds  The messages to delete
   */
  private int cleanDataFiles(MessageHandlerContext clientState, Collection<String> messageIds) {
    int count = 0;

    // the Sedex adapter's receipt directory
    File sentDir = new File(clientState.getClientConfiguration()
            .getSedexAdapterConfiguration().getSentDir());

    DirectoryStream<Path> dataFiles = FileUtils.listFiles(sentDir, FileFilters.XML_FILTER_PATH);

    for (Path path : dataFiles) {
      File file = path.toFile();
      Matcher matcher = SUFFIX_PATTERN.matcher(file.getName());
      if (!matcher.find()) {
        LOG.warn("a data file does not follow the naming convention: " + file.getAbsolutePath());
        continue; // try another file
      }

      final String suffix = matcher.group(1);

      if (messageIds.contains(suffix)) {
        // remove this file
        if (file.delete()) {
          count++;
          LOG.debug("the data file removed: " + file.getAbsolutePath());
        } else {
          LOG
                  .error("cannot remove the data file: " + file.getAbsolutePath());
        }
      }
    }
    try{
      dataFiles.close();
    } catch (IOException e){
      LOG.error("Unable to close stream directory " + e);
    }
    return count;
  }

}
