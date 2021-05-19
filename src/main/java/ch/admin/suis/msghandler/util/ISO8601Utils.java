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

package ch.admin.suis.msghandler.util;

import java.util.Date;
import org.apache.commons.lang.Validate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility class to provide some basic validations.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public final class ISO8601Utils {

  private static final DateTimeFormatter DATE_TIME_WITH_MS = ISODateTimeFormat.dateTime();

  private ISO8601Utils(){

  }

  /**
   * Tests whether the given string is in ISO-8601 format.
   *
   * @param value
   *          to be tested
   *
   * @throws NullPointerException
   *           if the provided value is <code>null</code>
   */
  public static boolean isISO8601Date(String value) {
    Validate.notNull(value);

    try {
      parse(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Parse the given string in the ISO-8601 format and returns the resulting
   * date. If the provided value is <code>null</code>, this method returns
   * <code>null</code> as well.
   *
   * @param value
   *          must be a string in the ISO-8601 format
   *
   * @return the created <code>java.util.Date</code> object
   * @throws IllegalArgumentException
   *           if the provided parameter value is not a string in the ISO-8601
   *           format
   * @throws UnsupportedOperationException
   *        if parsing is not supported
   */
  public static Date parse(String value) {
    if (null == value) {
      return null;
    }
   DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
   return fmt.parseDateTime(value).toDate();
  }

  /**
   * Formats the given date in the ISO-8601 format (yyyy-MM-ddTHH:mm:ss.SSS+HH:mm).
   *
   * @param date the date; if it is <code>null</code>, then this method returns <code>null</code>
   *
   * @return the formatted date or <code>null</code>
   */
  public static String format(Date date) {
    return null == date ? null : DATE_TIME_WITH_MS.print(date.getTime());
  }
}
