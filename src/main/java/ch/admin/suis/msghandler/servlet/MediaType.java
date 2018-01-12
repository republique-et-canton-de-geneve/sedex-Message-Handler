/*
 * $Id: MediaType.java 208 2012-07-31 09:42:56Z blaser $
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
package ch.admin.suis.msghandler.servlet;

/**
 * Interface which defines used HTTP Content-Types.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 0 $
 * @since 31.07.2012
 */
public interface MediaType {

	/**
	 * Text only
	 */
	String TEXT = "text/plain";

	/**
	 * Json
	 */
	String JSON = "application/json";

	/**
	 * Html
	 */
	String HTML = "text/html";

}