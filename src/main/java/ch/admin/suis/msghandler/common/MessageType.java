/*
 * $Id: MessageType.java 327 2014-01-27 13:07:13Z blaser $
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

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A class for value objects to represent the message types.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class MessageType {

	private int type;

	/**
	 * Constructor for a new MessageType
	 *
	 * @param type int representing the message type
	 */
	public MessageType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String toString() {
		return new DecimalFormat("####0").format(type);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof MessageType) {
			MessageType other = (MessageType) obj;
			return other.type == this.type;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(type).toHashCode();
	}

	/**
	 * Creates a list of message types from the provided string value. If the value
	 * is null or empty, an empty list is returned.
	 *
	 * @param typesValue the types in the form <code>type1 type2</code>, i.e. the types are
	 *                   numerical values separated by whitespaces
	 * @return List of message types from a message
	 */
	public static List<MessageType> from(String typesValue) {
		// read the message types
		final ArrayList<MessageType> messageTypes = new ArrayList<>();

		if (null != typesValue) {
			final StringTokenizer types = new StringTokenizer(typesValue);

			while (types.hasMoreTokens()) {
				String type = types.nextToken();
				messageTypes.add(new MessageType(Integer.decode(type)));
			}
		}

		return messageTypes;
	}

	/**
	 * Formats a collection of message types.
	 *
	 * @param types the collection of message types
	 * @return A concatenated string of message types
	 */
	public static String collectionToString(Collection<MessageType> types) {
		final StringBuilder typeResult = new StringBuilder();
		if (null != types) {
			for (Iterator<MessageType> i = types.iterator(); i.hasNext(); ) {
				typeResult.append(i.next().toString());
				if (i.hasNext()) {
					typeResult.append(", ");
				}
			}
		}

		return typeResult.toString();
	}
}
