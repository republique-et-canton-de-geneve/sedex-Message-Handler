/*
 * $Id: ClientCommons.java 327 2014-01-27 13:07:13Z blaser $
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

/**
 * The <code>ClientCommons</code> interface defines some static constants used by the
 * <code>StatusCheckerJob</code> and <code>SenderJob</code> classes.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public interface ClientCommons {

	String MESSAGE_ID = "messageId={0}";
	String RECIPIENT_ID = "recipientId={1}";
	String SENT = "sent={2}";

	/**
	 * line separator for the target system
	 */
	public String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * how the .prot files are normally formatted (for forwarded, sent, delivered)
	 */
	public String PROTOCOL_FORMAT_NORMAL = MESSAGE_ID + LINE_SEPARATOR
			+ RECIPIENT_ID + LINE_SEPARATOR + SENT + LINE_SEPARATOR
			+ "delivered={3}" + LINE_SEPARATOR;

	/**
	 * how to format the .prot files if the message is expired
	 */
	public String PROTOCOL_FORMAT_EXPIRED = MESSAGE_ID + LINE_SEPARATOR
			+ RECIPIENT_ID + LINE_SEPARATOR + SENT + LINE_SEPARATOR
			+ "expired={3}" + LINE_SEPARATOR;

	/**
	 * error format for the .err protocol files
	 */
	public String PROTOCOL_FORMAT_ERROR = MESSAGE_ID + LINE_SEPARATOR
			+ RECIPIENT_ID + LINE_SEPARATOR + SENT + LINE_SEPARATOR
			+ "errorCode={3}" + LINE_SEPARATOR
			+ "description={4}" + LINE_SEPARATOR;


	/**
	 * how to format the .prot file if the message cannot be sent due to an disallowed recipient
	 */
	public String NOT_SENT_FORMAT =
			"disallowed addressee={0}" + LINE_SEPARATOR +
					"discarded={1}";

	/**
	 * the SENT folder name
	 */
	public String SENT_DIR = "sent";

	/**
	 * the NOTSENT folder name
	 */
	public String NOT_SENT_DIR = "notsent";

	/**
	 * the name of the folder where the corrupted messages should be placed
	 */
	public String CORRUPTED_DIR = "corrupted";

	/**
	 * the name of the input folder where the messages for unknown recipients should be placed
	 */
	public String UNKNOWN_DIR = "unknown";

	/**
	 * the name of the folder where the incoming files should be temporary placed
	 */
	public String INBOX_TMP_DIR = "tmp/receiving";

	/**
	 * the name of the folder where the outcoming files should be temporary placed
	 */
	public String OUTBOX_TMP_DIR = "tmp/preparing";

	/**
	 * constant string 'not specified"
	 */
	public String NOT_SPECIFIED = "[not specified]";

}
