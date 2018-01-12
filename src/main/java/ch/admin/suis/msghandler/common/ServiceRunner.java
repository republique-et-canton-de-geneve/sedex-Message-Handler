/*
 * $Id: ServiceRunner.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.checker.StatusCheckerJob;
import ch.admin.suis.msghandler.config.Outbox;
import ch.admin.suis.msghandler.log.DbLogServiceJob;
import ch.admin.suis.msghandler.receiver.ReceiverJob;
import ch.admin.suis.msghandler.sender.SenderConfiguration;
import ch.admin.suis.msghandler.sender.SenderJob;
import ch.admin.suis.msghandler.sender.TransparentSenderJob;
import org.quartz.*;

import java.text.ParseException;
import java.util.List;

/**
 * This class executes the sender-receiver client by starting the sender-receiver jobs with cron job triggers.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class ServiceRunner implements ClientRunner {

	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(ServiceRunner.class.getName());

	private Scheduler scheduler;

	/*
	 *  (non-Javadoc)
	 * @see ch.admin.suis.goveai.client.common.ClientRunner#execute(ch.admin.suis.goveai.client.common.SenderReceiverClient.ClientState)
	 */
	@Override
	public void execute(MessageHandlerContext clientState) {
		// start the Quartz jobs

		LOG.info("starting the Quartz jobs");

		try {
			scheduler = clientState.getSchedulerFactory().getScheduler();

			// start them all
			scheduler.start();

			// sender jobs
			for (SenderConfiguration senderConfiguration : clientState.getClientConfiguration().getSenderConfigurations()) {

				List<Outbox> outboxes = senderConfiguration.getOutboxes();

				// we have one job class but multiple triggers and job details
				JobDetail jobDetailSender = new JobDetail("senderJob" + senderConfiguration.getName(),
						null,
						SenderJob.class);

				// setup the data map for the sender
				jobDetailSender.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, clientState);
				jobDetailSender.getJobDataMap().put(MessageHandlerContext.OUTBOX_PARAM, outboxes); // the list of outboxes

				final String name = "senderTrigger_" + senderConfiguration.getName();
				Trigger senderTrigger = new CronTrigger(name, null, senderConfiguration.getCron());

				// for each OUT box
				scheduler.scheduleJob(jobDetailSender, senderTrigger);
				LOG.info("sender started with name=" + name);
			}

			// transparent sender jobs
			for (SenderConfiguration senderConfiguration : clientState.getClientConfiguration().
					getTransparentSenderConfigurations()) {

				List<Outbox> outboxes = senderConfiguration.getOutboxes();

				// we have one job class but multiple triggers and job details
				JobDetail jobDetailSender = new JobDetail("transparentSenderJob" + senderConfiguration.getName(),
						null,
						TransparentSenderJob.class);

				// setup the data map for the sender
				jobDetailSender.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, clientState);
				jobDetailSender.getJobDataMap().put(MessageHandlerContext.OUTBOX_PARAM, outboxes); // the list of outboxes

				final String name = "transparentSenderTrigger_" + senderConfiguration.getName();
				Trigger senderTrigger = new CronTrigger(name, null, senderConfiguration.getCron());

				// for each OUT box
				scheduler.scheduleJob(jobDetailSender, senderTrigger);
				LOG.info("transparent sender started with name=" + name);
			}

			// receiver job
			JobDetail jobDetailReceiver = new JobDetail("receiverJob", null, ReceiverJob.class);

			jobDetailReceiver.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, clientState);
			final String receiverName = "receiverTrigger";
			Trigger receiverTrigger = new CronTrigger(receiverName, null, clientState.getClientConfiguration().
					getReceiverConfiguration().getCron());
			scheduler.scheduleJob(jobDetailReceiver, receiverTrigger);
			LOG.info("receiver started");

			// status checker job
			JobDetail jobDetailChecker = new JobDetail("checkerJob", null, StatusCheckerJob.class);

			Trigger checkerTrigger = new CronTrigger("checkerTrigger", null, clientState.getClientConfiguration().
					getStatusCheckerConfiguration().getCron());
			jobDetailChecker.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, clientState);
			scheduler.scheduleJob(jobDetailChecker, checkerTrigger);
			LOG.info("message status checker started");

			// schedule the cleanup thread for the DbLogService
			JobDetail jobDetailDbLog = new JobDetail("dbLogJob", null, DbLogServiceJob.class);

			// we make this trigger run at 00:00 every day
			Trigger dbLogTrigger = TriggerUtils.makeDailyTrigger("dbLogTrigger", 0, 0);

			jobDetailDbLog.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, clientState);
			scheduler.scheduleJob(jobDetailDbLog, dbLogTrigger);
			LOG.info("cleanup job for message status DB started");
		} catch (SchedulerException | ParseException e) {
			// something wrong with the scheduler or the cron expression is wrong
			LOG.error(e.getMessage(), e);
		}

		// this does not end the service client, since the scheduler overtakes the control
	}

	/**
	 * Stops the internal scheduler if it is initialized.
	 *
	 * @throws SchedulerException if an error occured while stopping the scheduler
	 */
	@Override
	public void stop() throws SchedulerException {
		if (null != scheduler) {
			scheduler.shutdown();
		}
	}
}
