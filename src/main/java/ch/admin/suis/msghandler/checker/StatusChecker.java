/*
 * $Id: StatusChecker.java 327 2014-01-27 13:07:13Z blaser $
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
 * The <code>StatusChecker</code> component checks the status of the sent messages
 * and updates it if there are the xml in the xml directory of the Sedex adapter.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class StatusChecker {
  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
          .getLogger(StatusChecker.class.getName());

  /**
   * Executes the status checker.
   *
   * @param session The session to check
   */
  void execute(StatusCheckerSession session) {

    Collection<Receipt> receipts;

    try {
      receipts = session.getMessagesIds();
    } catch (LogServiceException e1) {
      LOG.fatal("cannot access the internal log DB to get the list of sent messages; status checker stopped", e1);
      // we do not continue
      return;
    }

    for (Receipt receipt : receipts) {

      Semaphore defenseLock = session.getDefenseLock();
      // acquire the lock so that the stop message waits until we reach the
      // end of this block

      try {

        defenseLock.acquire();

        // everything inside this try-catch-finally block
        // represents a unit of work that must be completed
        // even if the sender is interrupted


        // try to receive a message
        session.updateStatus(receipt);


      } catch (InterruptedException interrupted) {
        LOG.warn("receiver is interrupted while acquiring the lock to perform its unit of work");
        Thread.currentThread().interrupt();
      } catch (LogServiceException e) {
        LOG.fatal("cannot access the internal log DB to update the status of the message with ID=" + receipt.getMessageId(), e);
        // we try the next message
      } finally {
        // and release the lock
        defenseLock.release();
      }
    }
    
    // SEDEX-260 Move receipt files with IDs of the messages not in internal DB
    moveReceiptFilesWithIDNotInDB(session);

    LOG.debug("checker completed");
  }

  private void moveReceiptFilesWithIDNotInDB(StatusCheckerSession session) {
      // SEDEX-260 Move receipt files with IDs of the messages not in internal DB
      Collection<Receipt> receiptsNotInSentIds;
      try {
	  receiptsNotInSentIds = session.getReceiptsNotInSentIds();
      } catch (LogServiceException e1) {
	  LOG.fatal("cannot access the internal log DB to get the list of sent messages; status checker stopped", e1);
	  // we do not continue
	  return;
      }
      for (Receipt receipt : receiptsNotInSentIds) {

	  Semaphore defenseLock = session.getDefenseLock();
	  // acquire the lock so that the stop message waits until we reach the
	  // end of this block

	  try {

	      defenseLock.acquire();

	      // everything inside this try-catch-finally block
	      // represents a unit of work that must be completed
	      // even if the sender is interrupted

	      // try to receive a message
	      session.move(receipt);

	  } catch (InterruptedException interrupted) {
	      LOG.warn("receiver is interrupted while acquiring the lock to perform its unit of work");
	      Thread.currentThread().interrupt();
	  } finally {
	      // and release the lock
	      defenseLock.release();
	  }
      }
  }

}
