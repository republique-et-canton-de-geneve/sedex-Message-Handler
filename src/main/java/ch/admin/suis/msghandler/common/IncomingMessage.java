/*
 * $Id: IncomingMessage.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.config.Inbox;

import java.util.ArrayList;
import java.util.List;


/**
 * The incoming messages contain the inbox object their are destined to.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class IncomingMessage {

	private final List<Inbox> inboxes = new ArrayList<>();
	private final Message message;

	/**
	 * Creates a new incoming message.
	 *
	 * @param message the underlying message object
	 */
	public IncomingMessage(Message message) {
		this.message = message;
	}

	/**
	 * Returns the inboxes, this message is destined to. Hint: A message can have multiple receivers so it's possible
	 * that there are multiple inboxes...
	 *
	 * @return Returns the inbox.
	 */
	public List<Inbox> getInboxes() {
		return inboxes;
	}

	public void addInbox(Inbox inbox) {
		inboxes.add(inbox);
	}

	/**
	 * Returns the message object.
	 *
	 * @return an incoming message.
	 */
	public Message getMessage() {
		return message;
	}

}
