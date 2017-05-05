/*
 * $Id: ReceiverJob.java 340 2015-08-16 14:51:19Z sasha $
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

package ch.admin.suis.msghandler.receiver;

import ch.admin.suis.msghandler.common.MessageHandlerContext;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.util.concurrent.Semaphore;

/**
 * The <code>ReceiverJob</code> starts the receiver process.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public class ReceiverJob implements StatefulJob {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ReceiverJob.class.getName());

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.debug("receiver job started");

		// get the objects that are necessary for the receiver
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		MessageHandlerContext clientState = (MessageHandlerContext) dataMap.get(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM);

		Semaphore sequenceLock = clientState.getSequenceLock();

		try {
			sequenceLock.acquire();

			try {
				new Receiver().execute(new ReceiverSessionImpl(clientState));
			} finally {
				sequenceLock.release();
			}
		} catch (InterruptedException e) {
			LOG.info("sender terminated while waiting for other jobs to complete");
			Thread.currentThread().interrupt();
		}

	}

}
