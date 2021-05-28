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

/**
 * Helper class to generate html code.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 20.07.2012
 */
public final class HtmlUtils {

  private HtmlUtils() {
  }

  /**
   * Generates a bit of html. Example: insert("h1" "hello world") will produce: "<h1>hello world</h1>".
   *
   * @param tag a html tag
   * @param value value which have to be enclose between the tag
   * @return valid html. Like: <tag>value</tag>"
   */
  public static String addTag(String tag, String value) {
    StringBuilder sb = new StringBuilder(tag.length() * 2 + 5 + value.length());
    sb.append("<");
    sb.append(tag);
    sb.append(">");
    sb.append(value);
    sb.append("</");
    sb.append(tag);
    sb.append(">");
    return sb.toString();
  }
}