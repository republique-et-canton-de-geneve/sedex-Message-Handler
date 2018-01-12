/*
 * $Id: SenderJob.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.sender;

import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.config.Outbox;
import org.apache.commons.lang.Validate;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The Quartz job to send files from a configured output directory.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class SenderJob implements StatefulJob {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SenderJob.class.getName());

	/*
	 * (non-Javadoc)
	 *
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.debug("sender job started");

		// get the objects that are necessary for the sender
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		final MessageHandlerContext handlerContext =
				(MessageHandlerContext) dataMap.get(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM);
		final List<Outbox> outbox = (List<Outbox>) dataMap.get(MessageHandlerContext.OUTBOX_PARAM);

		// pre-conditions check
		Validate.notNull(handlerContext);
		Validate.notNull(outbox);

		Semaphore sequenceLock = handlerContext.getSequenceLock();

		try {
			sequenceLock.acquire();

			try {
				if (this instanceof TransparentSenderJob) {
					new Sender().execute(new TransparentSenderSessionImpl(handlerContext, outbox));
				} else {
					new Sender().execute(new SenderSessionImpl(handlerContext, outbox));
				}
			} finally {
				sequenceLock.release();
			}
		} catch (InterruptedException e) {
			LOG.info("sender terminated while waiting for other jobs to complete");
			Thread.currentThread().interrupt();
		}
	}

}
