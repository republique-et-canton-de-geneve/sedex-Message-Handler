/*
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
 * $Id$
 */

package ch.admin.suis.msghandler.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for the {@link ISO8601Utils} class.
 *
 * @author      Alexander Nikiforov
 * @author      $Author$
 * @version     $Revision$
 */
public class ISO8601UtilsTest {

	@Test
  public void testIsISO8601Date() {
    assertTrue(ISO8601Utils.isISO8601Date("2009-03-10T13:35:26.075Z"));
    assertTrue(ISO8601Utils.isISO8601Date("2009-03-10T13:35:26.75Z"));
    assertTrue(ISO8601Utils.isISO8601Date("2009-03-10T13:35:26.7Z"));
    assertTrue(ISO8601Utils.isISO8601Date("2011-12-03T07:00:05.0+02:00"));
    assertTrue(ISO8601Utils.isISO8601Date("2011-12-03T07:00:05.00+02:00"));
    assertTrue(ISO8601Utils.isISO8601Date("2011-12-03T07:00:05.000+02:00"));
    assertTrue(ISO8601Utils.isISO8601Date("2011-12-03T07:00:05.000"));
    assertTrue(ISO8601Utils.isISO8601Date("2012-03-20T16:10:20.69+01:00"));
  }

}
