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
package ch.admin.suis.msghandler.common;

import org.quartz.SchedulerException;

/**
 * The <code>ClientRunner</code> interface describes how to execute the sender-receiver client.
 * The client delegates to this class its execution.
 *
 * @author      Alexander Nikiforov
 * @author      $Author$
 * @version     $Revision$
 */
public interface ClientRunner {

  /**
   * Executes the client. This method will be called from the client's <code>run</code>
   * method.
   *
   * @param state the client state
   */
  void execute(MessageHandlerContext state);

  /**
   * Stops the internal scheduler if it is initialized.
   *
   * @throws SchedulerException if an error occured while stopping the scheduler
   */
  void stop() throws SchedulerException;
}
