/*
 * $Id: TriggerServlet.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.servlet;

import ch.admin.suis.msghandler.checker.StatusCheckerJob;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.config.Outbox;
import ch.admin.suis.msghandler.receiver.ReceiverJob;
import ch.admin.suis.msghandler.sender.SenderConfiguration;
import ch.admin.suis.msghandler.sender.SenderJob;
import ch.admin.suis.msghandler.sender.TransparentSenderJob;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * <p> This servlet triggers the
 * <code>send</code>,
 * <code>receive</code> or
 * <code>poll</code> jobs for immediate execution. The parameters are <ul> <li><code>action</code> - can be send,
 * receive or poll</li> <li><code>name</code> - name of the outbox if the action is
 * <code>send</code></li> </ul>
 * <p>
 * <p> It is configured via Jetty embedded web-server. </p>
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class TriggerServlet extends HttpServlet implements MediaType {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TriggerServlet.class.getName());

	/**
	 * separate group for the jobs started by this servlet
	 */
	private static final String TRIGGER_GROUP = "triggerGroup";

	private Scheduler scheduler;

	private final MessageHandlerContext mhContext;

	/**
	 * The Trigger Servlet.
	 *
	 * @param mhContext the MessageHandlerContext
	 */
	public TriggerServlet(MessageHandlerContext mhContext) {
		super();

		this.mhContext = mhContext;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			scheduler = mhContext.getSchedulerFactory().getScheduler();
		} catch (SchedulerException e) {
			LOG.fatal("cannot initialize the scheduler for the trigger servlet", e);
			throw new ServletException(e);
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doProcess(request, response);
	}

	/**
	 * Processes the incoming requests.
	 *
	 * @param request  An HTTP Request.
	 * @param response The Response
	 * @throws ServletException Something went wront in the servlet !
	 * @throws IOException      Classical IO problems.
	 */
	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(TEXT);

		final String action = request.getParameter("action");

		try {
			if (StringUtils.equalsIgnoreCase(action, "send")) {
				dispatchSendRequest(request, response);
			} else if (StringUtils.equalsIgnoreCase(action, "receive")) {
				handleReceive(response);
			} else if (StringUtils.equalsIgnoreCase(action, "poll")) {
				handlePoll(response);
			} else {
				String msg = "No valid parameter found. Valid parameter: action={send,receive,poll}";
				LOG.warn(msg);
				response.getWriter().println(msg);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (SchedulerException ex) {
			String msg = "cannot trigger an immediate job";
			LOG.fatal(msg, ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException(msg, ex);
		}
	}

	/**
	 * Either sends the user an error informing him of a malformed request, or grants its request.
	 *
	 * @param request  The request
	 * @param response The response
	 * @throws IOException        IO problems...
	 * @throws SchedulerException The scheduler did not like what you said.
	 */
	private void dispatchSendRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException, SchedulerException {

		final String outboxName = request.getParameter("name");
		if (StringUtils.isNotBlank(outboxName)) {
			if (!handleSend(response, outboxName)) {
				String msg = "No outbox directory found with name: " + outboxName;
				LOG.warn(msg);
				response.getWriter().println(msg);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			String msg = "The action=send requires also 'name' parameter. Which is the name of the outbox which should be sent.";
			LOG.warn(msg);
			response.getWriter().println(msg);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private void handlePoll(HttpServletResponse response) throws IOException, SchedulerException {
		JobDetail jobDetailStatusChecker = new JobDetail("checkerJob" + System.currentTimeMillis(), TRIGGER_GROUP,
				StatusCheckerJob.class);

		jobDetailStatusChecker.getJobDataMap().put(
				MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, mhContext);

		// the job will be scheduled immediately, no repeats
		Trigger statusCheckerTrigger = new SimpleTrigger("simpleCheckerTrigger" + System.currentTimeMillis(), TRIGGER_GROUP);
		scheduler.scheduleJob(jobDetailStatusChecker, statusCheckerTrigger);

		response.getWriter().println("poll successfully started");
	}

	private void handleReceive(HttpServletResponse response) throws SchedulerException, IOException {
		JobDetail jobDetailReceiver = new JobDetail("receiverJob" + System.currentTimeMillis(), TRIGGER_GROUP,
				ReceiverJob.class);

		jobDetailReceiver.getJobDataMap().put(
				MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, mhContext);

		// the job will be scheduled immediately, no repeats
		Trigger receiverTrigger = new SimpleTrigger("simpleReceiverTrigger" + System.currentTimeMillis(), TRIGGER_GROUP);
		scheduler.scheduleJob(jobDetailReceiver, receiverTrigger);
		response.getWriter().println("receive action successfully started");
	}

	private boolean handleSend(HttpServletResponse response, String outboxName) throws SchedulerException, IOException {

		// loop over the configured outboxes and see which one should be emptyed
		for (SenderConfiguration senderConfiguration : mhContext.getClientConfiguration().getSenderConfigurations()) {
			List<Outbox> outboxes = senderConfiguration.getOutboxes();

			for (Outbox outbox : outboxes) {
				if (StringUtils.equalsIgnoreCase(outboxName, outbox.getName())) {
					// scheduling immediate sender job for this box
					JobDetail jobDetailSender = new JobDetail("senderJob" + outboxName + System.currentTimeMillis(), TRIGGER_GROUP,
							SenderJob.class);

					// setup the data map for the sender
					jobDetailSender.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, mhContext);
					jobDetailSender.getJobDataMap().put(MessageHandlerContext.OUTBOX_PARAM, Collections.singletonList(outbox)); // pack this outbox as list

					Trigger senderTrigger = new SimpleTrigger("simpleSenderTrigger" + outboxName + System.currentTimeMillis(),
							TRIGGER_GROUP);

					scheduler.scheduleJob(jobDetailSender, senderTrigger);
					response.getWriter().println("send successfully triggered");
					return true;
				}
			}
		}

		//Transparent senders
		for (SenderConfiguration senderConfiguration : mhContext.getClientConfiguration().
				getTransparentSenderConfigurations()) {
			List<Outbox> outboxes = senderConfiguration.getOutboxes();

			for (Outbox outbox : outboxes) {
				if (StringUtils.equalsIgnoreCase(outboxName, outbox.getName())) {
					// scheduling immediate sender job for this box
					JobDetail jobDetailSender = new JobDetail("transparentSenderJob" + outboxName + System.currentTimeMillis(),
							TRIGGER_GROUP, TransparentSenderJob.class);

					// setup the data map for the sender
					jobDetailSender.getJobDataMap().put(MessageHandlerContext.MESSAGE_HANDLER_CONTEXT_PARAM, mhContext);
					jobDetailSender.getJobDataMap().put(MessageHandlerContext.OUTBOX_PARAM, Collections.singletonList(outbox)); // pack this outbox as list

					Trigger senderTrigger = new SimpleTrigger("simpleTransparentSenderTrigger" + outboxName + System.
							currentTimeMillis(), TRIGGER_GROUP);

					scheduler.scheduleJob(jobDetailSender, senderTrigger);
					response.getWriter().println("send successfully triggered");
					return true;
				}
			}
		}

		return false; //no outbox found to trigger
	}
}
