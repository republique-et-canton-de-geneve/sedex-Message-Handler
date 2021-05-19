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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package ch.admin.suis.msghandler.signer;

/**
 * Exception used for Signer process.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 04.07.2012
 */
public class SignerException extends Exception {

  /**
   * Creates a new
   * <code>SignerException</code> with the given message.
   *
   * @param message
   */
  public SignerException(String message) {
    super(message);
  }

  /**
   * Creates a new
   * <code>SignerException</code> caused by the supplied exception.
   *
   * @param e
   */
  public SignerException(Throwable e) {
    super(e);
  }

  /**
   * Creates a new
   * <code>SignerException</code> with the given message and caused by the supplied exception.
   *
   * @param message
   * @param e
   */
  public SignerException(String message, Throwable e) {
    super(message, e);
  }
}