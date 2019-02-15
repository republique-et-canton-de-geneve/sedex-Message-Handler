/*
 * $Id: ReceiverSession.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.common.IncomingMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * The <code>ReceiverSession</code> contains methods to interact with
 * the environment.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public interface ReceiverSession {

	/**
	 * Returns the semaphore that should be acquired if the client wants to
	 * perform an operation without being interrupted by the message handler.
	 *
	 * @return The semaphore.
	 */
	Semaphore getDefenseLock();

	/**
	 * Returns a collection of new messages.
	 *
	 * @return A Collection of new messages (IncomingMessage)
	 */
	Collection<IncomingMessage> getNewMessages();

	/**
	 * Receives the message unpacking, if needed, the
	 * file.
	 *
	 * @param message The Incoming message to process.
	 * @throws IOException IO problems, usually related to permissions, for example.
	 */
	void receive(IncomingMessage message) throws IOException;

	/**
	 * Writes to the logs if the message has been successfully received
	 * and unpacked.
	 *
	 * @param message The message to stringify.
	 */
	void logSuccess(IncomingMessage message);

	/**
	 * Writes to the error log if the message could not be
	 * received because of an error
	 *
	 * @param message The message that caused a problem
	 * @param e       the exception triggered
	 */
	void logError(IncomingMessage message, Exception e);


	/**
	 * Removes the temporary files created by the session.
	 */
	void cleanup();
}
