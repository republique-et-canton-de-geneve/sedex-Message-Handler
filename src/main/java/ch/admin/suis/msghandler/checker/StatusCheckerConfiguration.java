/*
 * $Id: StatusCheckerConfiguration.java 327 2014-01-27 13:07:13Z blaser $
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

package ch.admin.suis.msghandler.checker;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.ReceiptsFolder;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Configuration parameters for the status checker process.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class StatusCheckerConfiguration {
	private String cron;

	private List<ReceiptsFolder> receiptsFolders = new ArrayList<>();

	/**
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
	 * Adds a receipt folder.
	 *
	 * @param folder The xml folder
	 */
	public void addReceiptFolder(ReceiptsFolder folder) {
		receiptsFolders.add(folder);
	}

	/**
	 * Get a list with all added receipt folders.
	 *
	 * @return A list of xml
	 */
	public List<ReceiptsFolder> getReceiptsFolders() {
		return receiptsFolders;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String toString() {
		final StringBuilder boxes = new StringBuilder();
		for (Iterator<ReceiptsFolder> i = receiptsFolders.iterator(); i.hasNext(); ) {
			boxes.append("\n\t").append(i.next().toString());
		}

		return MessageFormat.format("cron expression: {0}, folders: {1}", getCron(),
				StringUtils.defaultIfEmpty(boxes.toString(), ClientCommons.NOT_SPECIFIED));
	}


}
