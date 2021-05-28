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
package ch.admin.suis.msghandler.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility class to convert Date objects from/to XSD DateTime String.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 20.07.2012
 */
public final class DateUtils {

  private DateUtils() {
  }

  /**
   * Converts a Date object to the corresponding XSD DateTime String.
   *
   * @param date the date to parse
   * @return corresponding XSD DateTime
   */
  public static String dateToXsdDateTime(Date date)  {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(date);
    return DatatypeConverter.printDateTime(calendar);
  }

  /**
   * Converts an XSD DateTime String to the corresponding Date object.
   *
   * @param date XSD DateTime formatted String
   * @return the date object
   * @throws IllegalArgumentException if parameter is not XSD DateTime format
   */
  public static Date xsdDateTimeToDate(String date) throws IllegalArgumentException {
    Calendar calendar = DatatypeConverter.parseDateTime(date);
    return calendar.getTime();
  }

  public static XMLGregorianCalendar stringToXMLGregorianCalendar(String s) throws ParseException,
          DatatypeConfigurationException {
    XMLGregorianCalendar result = null;
    Date date;
    SimpleDateFormat simpleDateFormat;
    GregorianCalendar gregorianCalendar;
    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    date = simpleDateFormat.parse(s);
    gregorianCalendar =
            (GregorianCalendar) GregorianCalendar.getInstance();
    gregorianCalendar.setTime(date);
    result = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    return result;
  }
}