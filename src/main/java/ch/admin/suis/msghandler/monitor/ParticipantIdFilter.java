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
 */
package ch.admin.suis.msghandler.monitor;

import ch.admin.suis.msghandler.log.DBLogEntry;

/**
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 20.07.2012
 */
public class ParticipantIdFilter extends Filter {

  private final String participantId;

  /**
   * Filter for a given participantId.
   *
   * @param participantId the participantId which should be accepted. All other will not pass the filter. Not
   * case-sensitive
   */
  public ParticipantIdFilter(String participantId) {
    this.participantId = participantId;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  protected boolean filter(DBLogEntry entry) {
    return entry.getRecipientId().equalsIgnoreCase(participantId);
  }
}