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

package ch.admin.suis.msghandler.sender;

import ch.admin.suis.msghandler.config.Outbox;
import ch.admin.suis.msghandler.naming.NamingService;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.Validate;


/**
 * The <code>SenderConfiguration</code> contains the parameters with which the
 * sender is configured. These include the absolute path to the directory where the
 * log database files are located and the configured applications.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class SenderConfiguration {
  private List<Outbox> outboxes = new ArrayList<Outbox>();

  private String cron;

  /**
   * Creates a new sender configuration object for the given
   * cron string.
   *
   * @param cron
   */
  public SenderConfiguration(String cron) {
    Validate.notEmpty(cron, "cron expression cannot be empty");
    this.cron = cron;
  }

  /**
   * Returns the cron string configured for this sender.
   *
   * @return
   */
  public String getCron() {
    return cron;
  }

  /**
   * Adds a new outbox to this configuration.
   *
   * @param outbox
   */
  public void addOutbox(Outbox outbox) {
    outboxes.add(outbox);
  }

  /**
   * Returns the outboxes configured for the sender. If there are no
   * outboxes, this method returns an empty list.
   *
   * @return
   */
  public List<Outbox> getOutboxes() {
    return outboxes;
  }

  /**
   * Returns a (unique) name for this configuration.
   *
   * @return
   */
  public String getName() {
    final StringBuilder result = new StringBuilder("");
    for (Outbox outbox : outboxes) {
      result.append("_").append(outbox.getDirectory());
    }

    return result.toString();
  }

  @Override
  public String toString() {
    final StringBuilder boxes = new StringBuilder();
    for (Iterator<Outbox> i = outboxes.iterator(); i.hasNext(); ) {
      boxes.append("\n\t").append(i.next().toString());
    }

    return MessageFormat.format("name: {0}; cron expression: {1}, outboxes: {2}",
        getName(),
        getCron(),
        boxes.toString()
        );
  }
}
