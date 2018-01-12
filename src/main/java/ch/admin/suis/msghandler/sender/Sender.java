/*
 * $Id: Sender.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.log.LogServiceException;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * The <code>Sender</code> class executes the sending for the given client and
 * the provided out box.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class Sender {
	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(Sender.class.getName());

	/**
	 * Executes a sending round for the specified out box.
	 */
	public void execute(SenderSession session) {

		try {
			// we create a list of files deemed to be sent
			// prepare the messages passing the state (prepared files over)
			// the stop signal can still interrupt it
			Collection<Message> messages = session.createMessages();
			handleMessages(session, messages);
		} finally {
			// cleanup for the message that has just been sent
			session.cleanup();
		}
	}

	private void handleMessages(SenderSession session, Collection<Message> messages) {
		for (Message message : messages) {

			Semaphore defenseLock = session.getDefenseLock();
			// acquire the lock so that the stop message waits until we reach the
			// end of this block

			try {

				defenseLock.acquire();

				// try to send a message
				session.send(message);

				// and if everything is ok, than log this
				session.logSuccess(message);
			} catch (IOException e) {
				// move away the files that were not sent
				session.logError(message, e);
			} catch (LogServiceException | InterruptedException e) {
				LOG.warn("sender is interrupted while acquiring the lock to perform its unit of work. Error : " + e);
			} finally {
				// and release the lock
				defenseLock.release();

				LOG.debug("sender completed");
			}
		}
	}
}
