/*
 * $Id: ProtocolService.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.protocol;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.Receipt;


/**
 * The <code>ProtocolService</code> serves to protocol the events either in a
 * database or in the text file.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public interface ProtocolService {

	/**
	 * Logs the message received event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param message  the message object describing the message the file has been received with
	 */
	void logReceived(String filename, Message message);

	/**
	 * Logs the message receiving event with the provided parameters. If an error
	 * occurs this method simply returns.
	 *
	 * @param filename the name of the file that has been rejected
	 * @param message  the message object describing the message the file has been received with
	 */
	void logReceiving(String filename, Message message);

	/**
	 * Logs the error event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param receipt  the message object describing the message the file has been received with
	 */
	void logError(String filename, Receipt receipt);

	/**
	 * Logs the message sent event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param receipt  the message object describing the message the file has been sent with
	 */
	void logSent(String filename, Receipt receipt);

	/**
	 * Logs the message preparing event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param message  the message object describing the message the file has been sent with
	 */
	void logPreparing(String filename, Message message);

	/**
	 * Logs the message forwarded event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param message  the message object describing the message the file has been sent with
	 */
	void logForwarded(String filename, Message message);

	/**
	 * Logs the message delivered event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param receipt  the message object describing the message the file has been sent with
	 */
	void logDelivered(String filename, Receipt receipt);

	/**
	 * Logs the message expired event with the provided parameters. If an error
	 * occurs, this method simply returns.
	 *
	 * @param filename the name of the file that has been sent or received
	 * @param receipt  the message object describing the message the file has been sent with
	 */
	void logExpired(String filename, Receipt receipt);

}
