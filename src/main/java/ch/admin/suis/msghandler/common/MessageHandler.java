/*
 * $Id: MessageHandler.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.config.ClientConfiguration;
import ch.admin.suis.msghandler.log.DbLogService;
import ch.admin.suis.msghandler.log.LogServiceConfiguration;
import ch.admin.suis.msghandler.log.LogServiceException;
import ch.admin.suis.msghandler.protocol.TextProtocolService;
import ch.admin.suis.msghandler.servlet.*;
import org.apache.commons.lang.StringUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class describes the <code>sender-receiver</code> process.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public final class MessageHandler implements Runnable {


	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(MessageHandler.class.getName());

	private MessageHandlerContext messageHandlerContext = new MessageHandlerContext();

	private Server server;

	private ClientRunner clientRunner;

	/**
	 * Instantiates the client.
	 *
	 * @param clientRunner the strategy how to execute the client
	 */
	public MessageHandler(ClientRunner clientRunner) {
		this.clientRunner = clientRunner;
	}

	/**
	 * Initializes the message hadler process.
	 *
	 * @throws IOException         if an error occurs while reading the client certificates
	 * @throws LogServiceException if the logging service cannot be started
	 */
	public void init(ClientConfiguration clientConfiguration) throws LogServiceException {
		messageHandlerContext.setClientConfiguration(clientConfiguration);

		// configure the protocol
		LOG.info("initializing the protocol service via Log4j interface");
		messageHandlerContext.setProtocolService(new TextProtocolService());

		// configure the log service
		LOG.info("initializing the message status service (DbLog)");
		DbLogService logService = new DbLogService();
		messageHandlerContext.setLogService(logService);

		LogServiceConfiguration logServiceConfiguration = clientConfiguration.getLogServiceConfiguration();
		logService.setBase(logServiceConfiguration.getLogBase());
		logService.setMaxAge(logServiceConfiguration.getMaxAge());
		logService.setResend(logServiceConfiguration.getResend());

		try {
			logService.init();
			LOG.info("the message status service (DbLog) initialized");
		} catch (SQLException e1) {
			throw new LogServiceException("log service cannot be initialized: " + e1);
		}

		// Jetty HTTP server
		try {
			initServer(clientConfiguration.getCommandInterfaceConfiguration());
		} catch (Exception e) {
			LOG.fatal("HTTP server cannot be started: " + e);
		}
	}

	/**
	 * Stops the client.
	 */
	public int stop() {
		// check if we are not interrupting some important task
		int returnCode = 0;

		try {
			// TODO find out what is the value of the WrapperManager timeout and use then tryAcquire
			messageHandlerContext.getDefenseLock().acquire();

			stopScheduler();
			// dump the log content
			((DbLogService) messageHandlerContext.getLogService()).dump();

			// stop the log service
			((DbLogService) messageHandlerContext.getLogService()).destroy();

			// stop the Jetty HTTP server
			if (server != null) {
				server.stop();
				LOG.info("HTTP server terminated");
			}
		} catch (InterruptedException e) {
			// this should not happen
			LOG.error("abnormal receiver termination");
			returnCode = -1;
		} catch (Exception e) {
			// we get this when the Jetty server cannot be stopped
			LOG.error("abnormal Jetty server termination", e);
			// we set the return code to signal the problem
			returnCode = -1;
		} finally {
			// just to be nice
			messageHandlerContext.getDefenseLock().release();
		}

		return returnCode;
	}

	private int stopScheduler() {
		// stop the scheduler and its jobs
		try {
			clientRunner.stop();
		} catch (SchedulerException e) {
			LOG.error("an error occured while stopping the scheduler: " + e);
			return -1;
		}
		return 0;
	}

	/**
	 * Schedules the Quartz jobs and waits for the stop signal.
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		clientRunner.execute(messageHandlerContext);
	}

	/**
	 * Initializes the HTTP Jetty server with the given configuration. If the given configuration
	 * does not contain the host name, this method simply returns and does not setup the server.
	 *
	 * @param commandInterfaceConfiguration The Jetty Configuration
	 * @throws Exception Some errors...
	 */
	private void initServer(CommandInterfaceConfiguration commandInterfaceConfiguration) throws Exception {
		if (StringUtils.isBlank(commandInterfaceConfiguration.getHost())) {
			return;
		}

		// start the Jetty HTTP server
		server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(commandInterfaceConfiguration.getPort());
		connector.setHost(commandInterfaceConfiguration.getHost());

		server.addConnector(connector);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		server.setHandler(contexts);

		Context senderReceiverContext = new Context(contexts, "/message-handler");

		// configure the filter for the servlet instance
		TriggerServlet triggerServlet = new TriggerServlet(messageHandlerContext);
		MonitorServlet monitorServlet = new MonitorServlet(messageHandlerContext);
		PingServlet pingServlet = new PingServlet();
		BaseServlet baseServlet = new BaseServlet();

		// and add it to the servlet context
		senderReceiverContext.addServlet(new ServletHolder(triggerServlet), "/trigger/*");
		senderReceiverContext.addServlet(new ServletHolder(monitorServlet), "/monitor/*");
		senderReceiverContext.addServlet(new ServletHolder(pingServlet), "/ping/*");
		senderReceiverContext.addServlet(new ServletHolder(baseServlet), "/*");

		server.start();

		LOG.info("HTTP server started on host " + commandInterfaceConfiguration.getHost() + "  and port " + commandInterfaceConfiguration.getPort());
	}

}
