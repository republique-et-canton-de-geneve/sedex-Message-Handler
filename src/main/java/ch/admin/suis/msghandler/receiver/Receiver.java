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

package ch.admin.suis.msghandler.receiver;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.IncomingMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * The <code>Sender</code> class contain the logic to receive messages on behalf of the given client.
 *
 * @author      Alexander Nikiforov
 * @author      $Author$
 * @version     $Revision$
 */
public class Receiver implements ClientCommons {
  /** logger */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
      .getLogger(Receiver.class.getName());

  /**
   * Creates a new instance of the <code>Receiver</code>. The instances of this
   * class are reusable, but other processes can change the content of the
   * provided client state object.
   */
  public Receiver() {
  }

  void execute(ReceiverSession session) {

    try {
      Collection<IncomingMessage> messages = session.getNewMessages();

      for (IncomingMessage message : messages) {

        Semaphore defenseLock = session.getDefenseLock();
        // acquire the lock so that the stop message waits until we reach the
        // end of this block

        try {

          defenseLock.acquire();

          // everything inside this try-catch-finally block
          // represents a unit of work that must be completed
          // even if the receiver is interrupted

          try {
            // try to receive a message
            session.receive(message);

            session.logSuccess(message);
          }
          catch (IOException e) {
            session.logError(message, e);
          }
          finally {
            // and release the lock
            defenseLock.release();

            LOG.debug("receiver completed");
          }
        }
        catch (InterruptedException interrupted) {
          LOG.warn("receiver is interrupted while acquiring the lock to perform its unit of work");
        }
      }
    }
    finally {
      session.cleanup();
    }
  }

}
