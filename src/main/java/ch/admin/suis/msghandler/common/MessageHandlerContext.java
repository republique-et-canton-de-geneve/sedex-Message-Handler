/*
 * $Id: MessageHandlerContext.java 327 2014-01-27 13:07:13Z blaser $
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
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.protocol.ProtocolService;

import java.util.concurrent.Semaphore;

import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Internal state of this message handler. It is introduced to enforce more
 * type-safety while pasing the client's state to the Quartz jobs as untyped
 * hashtable parameters.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class MessageHandlerContext {
	public static final String MESSAGE_HANDLER_CONTEXT_PARAM = "messageHandlerContext";

	/**
	 * outbox object configured for the given sender
	 */
	public static final String OUTBOX_PARAM = "outbox";

	private ClientConfiguration clientConfiguration;

	private LogService logService;

	private ProtocolService protocolService;

	private SchedulerFactory schedulerFactory = new StdSchedulerFactory();

	/**
	 * the <code>stop</code> method should wait and proceed only upon acquiring
	 * this; the jobs can take this lock when performing critical parts that
	 * cannot be interrupted by some external process
	 */
	private Semaphore defenseLock = new Semaphore(1);

	/**
	 * the operations should wait to acquire this lock; this is a means to provide
	 * serialability for the jobs
	 */
	private Semaphore sequenceLock = new Semaphore(1, true); // it is fair (fifo)

	/**
	 * @return Returns the clientConfiguration.
	 */
	public ClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}

	/**
	 * @return Returns the defenseLock.
	 */
	public Semaphore getDefenseLock() {
		return defenseLock;
	}

	/**
	 * @return Returns the logService.
	 */
	public LogService getLogService() {
		return logService;
	}

	/**
	 * @return Returns the sequenceLock.
	 */
	public Semaphore getSequenceLock() {
		return sequenceLock;
	}

	/**
	 * @return Returns the protocolService.
	 */
	public ProtocolService getProtocolService() {
		return protocolService;
	}

	/**
	 * Returns the scheduler factory to access the scheduler.
	 *
	 * @return SchedulerFactory
	 */
	public SchedulerFactory getSchedulerFactory() {
		return schedulerFactory;
	}

	/**
	 * @param clientConfiguration The clientConfiguration to set.
	 */
	public void setClientConfiguration(ClientConfiguration clientConfiguration) {
		this.clientConfiguration = clientConfiguration;
	}

	/**
	 * @param defenseLock The defenseLock to set.
	 */
	public void setDefenseLock(Semaphore defenseLock) {
		this.defenseLock = defenseLock;
	}

	/**
	 * @param logService The logService to set.
	 */
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	/**
	 * @param protocolService The protocolService to set.
	 */
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}

	/**
	 * @param schedulerFactory The schedulerFactory to set.
	 */
	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		this.schedulerFactory = schedulerFactory;
	}

	/**
	 * @param sequenceLock The sequenceLock to set.
	 */
	public void setSequenceLock(Semaphore sequenceLock) {
		this.sequenceLock = sequenceLock;
	}

}
