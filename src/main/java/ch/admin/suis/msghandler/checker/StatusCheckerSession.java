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

package ch.admin.suis.msghandler.checker;

import ch.admin.suis.msghandler.common.Receipt;
import ch.admin.suis.msghandler.log.LogServiceException;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * The <code>StatusCheckerSession</code> contains methods to interact with the
 * environment.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public interface StatusCheckerSession {

  /**
   * Returns the semaphore that should be acquired if the client wants to
   * perform an operation without being interrupted by the message handler.
   *
   * @return
   */
  Semaphore getDefenseLock();

  /**
   * Returns a collection of IDs of the messages whose status should be checked.
   *
   * @return
   * @throws LogServiceException
   *           if the internal log cannot return the ids
   */
  Collection<Receipt> getMessagesIds() throws LogServiceException;

  /**
   * Updates if possible the status of the files sent with the given message ID.
   *
   * @param receipt
   * @throws LogServiceException
   *           if the internal log cannot be accessed to change the status of
   *           the message with the provided ID
   */
  void updateStatus(Receipt receipt) throws LogServiceException;

}
