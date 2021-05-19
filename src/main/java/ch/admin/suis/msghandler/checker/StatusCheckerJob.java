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

package ch.admin.suis.msghandler.checker;

import ch.admin.suis.msghandler.common.MessageHandlerContext;
import java.util.concurrent.Semaphore;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * The <code>StatusCheckerJob</code> checks the status of the messages sent
 * by the local participant. This job can be interrupted by the stop signal.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class StatusCheckerJob implements StatefulJob {
  /** logger */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
      .getLogger(StatusCheckerJob.class.getName());

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.debug("status checker job started");
    // get the objects that are necessary for the sender
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    MessageHandlerContext clientState = (MessageHandlerContext) dataMap.get(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM);

    Semaphore sequenceLock = clientState.getSequenceLock();

    try {
      sequenceLock.acquire();

      try {
        // replace null with a concrete implementation of the StatusCheckerSession interface
        new StatusChecker().execute(new StatusCheckerSessionImpl(clientState));
      }
      finally {
        sequenceLock.release();
      }
    }
    catch (InterruptedException e) {
      LOG.info("status checker terminated while waiting for other jobs to complete");
    }

  }

}
