/*
 * $Id: MessageHandlerService.java 340 2015-08-16 14:51:19Z sasha $
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
import ch.admin.suis.msghandler.config.ClientConfigurationFactory;
import ch.admin.suis.msghandler.log.LogServiceException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.PropertyConfigurator;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.File;


/**
 * The <code>MessageHandlerService</code> represents entry point for the
 * <code>MessageHandler</code> service.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public final class MessageHandlerService implements WrapperListener {

	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MessageHandlerService.class
			.getName());


	/**
	 * Update this variable if you change the pom.xml artifactId!
	 */
	public static final String PRODUCT_NAME = "open-egov-msghandler";
	public static final String PRODUCT_VERSION = "3.4.0";
	private MessageHandler client = new MessageHandler(new ServiceRunner());

	/**
	 * The start method is called when the WrapperManager is signaled by the
	 * native wrapper code that it can start its application. This method call is
	 * expected to return, so a new thread should be launched if necessary.
	 *
	 * @param args List of arguments used to initialize the application.
	 * @return Any error code if the application should exit on completion of the
	 * start method. If there were no problems then this method should
	 * return null.
	 */
	@Override
	public Integer start(String[] args) {
		final String configPath = args[0]; // relative path to config.xml

		LOG.info("+-------------------------------------+");
		LOG.info(PRODUCT_NAME + " " + PRODUCT_VERSION);
		LOG.info("+-------------------------------------+");

		LOG.info("configuring the message handler service from the configuration file " + configPath);

		ClientConfigurationFactory factory;

		File configFile = new File(configPath);
		if (!configFile.exists()) {
			LOG.fatal("Config file not found: " + configFile.getAbsolutePath());
			return -1;
		}

		try {
			factory = new ClientConfigurationFactory(configPath);
			factory.init();
		} catch (ConfigurationException e) {
			// do not start if the configuration is wrong
			LOG.fatal("configuration file not found or cannot be parsed", e);
			return -1;
		}

		// we can configure the timeout value for the wrapper manager
		// with wrapper.startup.timeout property
		// by default it is 30s

		// initialize the service client
		ClientConfiguration clientConfiguration = factory.getClientConfiguration();
		try {
			client.init(clientConfiguration);
			LOG.info("SUIS message handler service initialized");
		} catch (LogServiceException e) {
			LOG.fatal("Log Service encoutered a problem . " + e);
			return -1;
		}

		// start the process
		new Thread(client).start();

		LOG.info("SUIS message handler service started");

		// and return null since there are no errors
		return null;
	}

	/**
	 * Called when the application is shutting down. The Wrapper assumes that this
	 * method will return fairly quickly. If the shutdown code code could
	 * potentially take a long time, then WrapperManager.signalStopping() should
	 * be called to extend the timeout period. If for some reason, the stop method
	 * can not return, then it must call WrapperManager.stopped() to avoid warning
	 * messages from the Wrapper.
	 *
	 * @param exitCode The suggested exit code that will be returned to the OS when the
	 *                 JVM exits.
	 * @return The exit code to actually return to the OS. In most cases, this
	 * should just be the value of exitCode, however the user code has the
	 * option of changing the exit code if there are any problems during
	 * shutdown.
	 */
	@Override
	public int stop(int exitCode) {
		LOG.info("stopping the message handler service");

		int clientExitCode = client.stop();
		LOG.info("the message handler service stopped with the exit code " + exitCode);

		// we return the code given us by the client
		return clientExitCode;
	}

	/**
	 * Called whenever the native wrapper code traps a system control signal
	 * against the Java process. It is up to the callback to take any actions
	 * necessary. Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
	 * WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or
	 * WRAPPER_CTRL_SHUTDOWN_EVENT
	 *
	 * @param event The system control signal.
	 */
	@Override
	public void controlEvent(int event) {
		if (WrapperManager.isControlledByNativeWrapper()) {
			return;
		}
		// We are not being controlled by the Wrapper, so
		// handle the event ourselves.
		if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT) || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
				|| (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT)) {
			WrapperManager.stop(0);
		}
	}

	/**
	 * @param args arguments to start the MsgHandlerService accordingly.
	 */
	public static void main(String[] args) {
		// initialize the log4j to watch every minute (by default)
		PropertyConfigurator.configureAndWatch(System.getProperty("log4j.configuration", "log4j.properties"));

		final MessageHandlerService service = new MessageHandlerService();

		// start the receiver process if everything is ok
		WrapperManager.start(service, args);
	}
}
