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

import ch.admin.suis.msghandler.config.Inbox;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * The <code>ReceiverConfiguration</code> contains configuration parameters
 * for the receiver.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class ReceiverConfiguration {
  private String cron;
  private List<Inbox> inboxes = new ArrayList<Inbox>();

  /**
   * Returns the string describing how often the receiver process should be started.
   *
   * @return Returns the cron.
   */
  public String getCron() {
    return cron;
  }

  /**
   * @param cron The cron to set.
   */
  public void setCron(String cron) {
    this.cron = cron;
  }

  /**
   * Adds another inbox to this configuration.
   *
   * @param inbox
   */
  public void addInbox(Inbox inbox) {
    inboxes.add(inbox);
  }

  /**
   * Returns all the configured inboxes.
   *
   * @return
   */
  public List<Inbox> getInboxes() {
    return inboxes;
  }

  @Override
  public String toString() {
    final StringBuilder boxes = new StringBuilder();
    for (Iterator<Inbox> i = inboxes.iterator(); i.hasNext(); ) {
      boxes.append("\n\t").append(i.next().toString());
    }

    return MessageFormat.format("cron expression: {0}, inboxes: {1}", getCron(), boxes.toString());
  }

}
