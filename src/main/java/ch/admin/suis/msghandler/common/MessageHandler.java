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

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.quartz.SchedulerException;

import ch.admin.suis.msghandler.config.ClientConfiguration;
import ch.admin.suis.msghandler.log.DbLogService;
import ch.admin.suis.msghandler.log.LogServiceConfiguration;
import ch.admin.suis.msghandler.protocol.TextProtocolService;
import ch.admin.suis.msghandler.servlet.BaseServlet;
import ch.admin.suis.msghandler.servlet.CommandInterfaceConfiguration;
import ch.admin.suis.msghandler.servlet.MonitorServlet;
import ch.admin.suis.msghandler.servlet.PingServlet;
import ch.admin.suis.msghandler.servlet.TriggerServlet;

/**
 * This class describes the <code>sender-receiver</code> process.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public final class MessageHandler implements Runnable
{

  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
      .getLogger(MessageHandler.class.getName());

  private MessageHandlerContext messageHandlerContext = new MessageHandlerContext();

  private Server server;

  private DbLogService logService;

  private ClientRunner clientRunner;

  /**
   * Instantiates the client.
   *
   * @param clientRunner
   *     the strategy how to execute the client
   */
  public MessageHandler(ClientRunner clientRunner)
  {
    this.clientRunner = clientRunner;
  }

  /**
   * Initializes the message hadler process.
   */
  public void init(ClientConfiguration clientConfiguration)
  {
    messageHandlerContext.setClientConfiguration(clientConfiguration);

    // configure the protocol
    LOG.info("initializing the protocol service via Log4j interface");
    messageHandlerContext.setProtocolService(new TextProtocolService());

    // configure the log service
    logService = configureLogService(clientConfiguration);
    messageHandlerContext.setLogService(logService);

    // Jetty HTTP server
    configureHttpServer(clientConfiguration.getCommandInterfaceConfiguration());
  }

  /**
   * Stops the client.
   */
  public int stop()
  {
    // check if we are not interrupting some important task
    int returnCode = 0;

    try
    {
      // TODO find out what is the value of the WrapperManager timeout and use then tryAcquire
      messageHandlerContext.getDefenseLock().acquire();

      // stop the scheduler and its jobs
      try
      {
        clientRunner.stop();
      } catch (SchedulerException e)
      {
        LOG.error("an error occured while stopping the scheduler: " + e.getMessage());
        returnCode = -1;
      }

      if (null != logService)
      {
        // dump the log content
        logService.dump();

        // stop the log service
        logService.destroy();
      }

      // stop the Jetty HTTP server
      if (null != server)
      {
        server.stop();
        LOG.info("HTTP server terminated");
      }
    } catch (InterruptedException e)
    {
      // this should not happen
      LOG.error("abnormal receiver termination");
      // do something with the exit code
      returnCode = -1;
    } catch (Exception e)
    {
      // we get this when the Jetty server cannot be stopped
      LOG.error("abnormal Jetty server termination", e);
      // we set the return code to signal the problem
      returnCode = -1;
    } finally
    {
      // just to be nice
      messageHandlerContext.getDefenseLock().release();
    }

    // ok
    return returnCode;
  }

  /**
   * Schedules the Quartz jobs and waits for the stop signal.
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    startLogService();
    startServer();

    clientRunner.execute(messageHandlerContext);
  }

  private void startLogService()
  {
    LOG.info("starting the message status service (DbLog)");
    try
    {
      logService.init();
      LOG.info("the message status service (DbLog) started");
    } catch (SQLException e)
    {
      throw new IllegalStateException("log service cannot be started: " + e);
    }
  }

  private DbLogService configureLogService(ClientConfiguration clientConfiguration)
  {
    LOG.info("configuring the message status service (DbLog)");
    DbLogService logService = new DbLogService();

    LogServiceConfiguration logServiceConfiguration = clientConfiguration.getLogServiceConfiguration();
    logService.setBase(logServiceConfiguration.getLogBase());
    logService.setMaxAge(logServiceConfiguration.getMaxAge());
    logService.setResend(logServiceConfiguration.getResend());
    return logService;
  }

  /**
   * Initializes the HTTP Jetty server with the given configuration. If the given configuration
   * does not contain the host name, this method simply returns and does not setup the server.
   */
  private void configureHttpServer(CommandInterfaceConfiguration commandInterfaceConfiguration)
  {
    final String host = commandInterfaceConfiguration.getHost();
    if (StringUtils.isBlank(host))
    {
      LOG.info("HTTP server is not configured");
      return;
    }

    final int port = commandInterfaceConfiguration.getPort();

    // start the Jetty HTTP server
    server = new Server();
    ServerConnector connector = new ServerConnector(server);

    connector.setPort(port);
    connector.setHost(host);

    server.addConnector(connector);

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);

		ServletContextHandler senderReceiverContext = new ServletContextHandler(
				contexts, "/message-handler");

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

    LOG.info(String.format("HTTP server configured to run on host %s and port %d", host, port));
  }

  private void startServer()
  {
    if (server != null)
    {
      LOG.info("starting the HTTP server");

      try
      {
        server.start();
        LOG.info("HTTP server started");
      } catch (Exception e)
      {
        LOG.fatal("HTTP server cannot be started: " + e);
        // this exception is suppressed since the message handler probably remains functioning
      }
    }
  }
}
