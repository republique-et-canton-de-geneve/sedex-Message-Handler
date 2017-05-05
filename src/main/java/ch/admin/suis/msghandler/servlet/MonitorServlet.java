/*
 * $Id: MonitorServlet.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.log.DBLogEntry;
import ch.admin.suis.msghandler.log.LogServiceException;
import ch.admin.suis.msghandler.log.LogStatus;
import ch.admin.suis.msghandler.monitor.*;
import ch.admin.suis.msghandler.util.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * The Monitor Servlet. Provides information about the processed files through the MessageHandler.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 18.07.2012
 */
public class MonitorServlet extends HttpServlet implements MediaType {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MonitorServlet.class.getName());

	private static final String PARAM_FROM = "from";

	private static final String PARAM_UNTIL = "until";

	private static final String PARAM_STATE = "state";

	private static final String PARAM_FILENAME = "filename";

	private static final String PARAM_SEDEX_ID = "sedexId";

	private static final String PARAM_MESSAGE_ID = "messageId";

	private final MessageHandlerContext mhContext;

	/**
	 * The Monitor Servlet. Provides information about the processed files through the MessageHandler.
	 *
	 * @param mhContext the MessageHandlerContext
	 */
	public MonitorServlet(MessageHandlerContext mhContext) {
		this.mhContext = mhContext;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("Get Request: " + request);
		doProcess(request, response);
	}

	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			FilterClient filterClient = handleParam(request);
			List<DBLogEntry> dbLogEntries = filterClient.filter(mhContext.getLogService().getAllEntries());
			response.getWriter().println(toJson(dbLogEntries, dbLogEntries.getClass()));
			response.setContentType(JSON);
			response.setStatus(HttpServletResponse.SC_OK);

		} catch (LogServiceException ex) {
			LOG.error(ex.getMessage(), ex);
			throw new ServletException(ex);

		} catch (MonitorException ex) {
			LOG.error("Unable to process the task: " + ex.getMessage(), ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType(TEXT);
			response.getWriter().println("Invalid request: " + ex.getMessage());

		} catch (IOException ex) {
			LOG.fatal("MonitorServlet: " + ex.getMessage(), ex);
			throw ex;
		}
	}

	private FilterClient handleParam(HttpServletRequest request) throws MonitorException {

		FilterClient filterClient = new FilterClient();
		String lastProcessedParam = null; //just for exception handling

		try {
			if (StringUtils.isNotBlank(request.getParameter(PARAM_FROM))) {
				lastProcessedParam = PARAM_FROM;
				filterClient.addFilter(new FromFilter(DateUtils.xsdDateTimeToDate(request.getParameter(PARAM_FROM))));
			}

			if (StringUtils.isNotBlank(request.getParameter(PARAM_UNTIL))) {
				lastProcessedParam = PARAM_UNTIL;
				filterClient.addFilter(new UntilFilter(DateUtils.xsdDateTimeToDate(request.getParameter(PARAM_UNTIL))));
			}

			if (StringUtils.isNotBlank(request.getParameter(PARAM_STATE))) {
				lastProcessedParam = PARAM_STATE;
				String sState = request.getParameter(PARAM_STATE).toUpperCase();
				filterClient.addFilter(new StateFilter(LogStatus.valueOf(sState)));
			}

			if (StringUtils.isNotBlank(request.getParameter(PARAM_FILENAME))) {
				lastProcessedParam = PARAM_FILENAME;
				filterClient.addFilter(new FileNameFilter(request.getParameter(PARAM_FILENAME)));
			}

			if (StringUtils.isNotBlank(request.getParameter(PARAM_SEDEX_ID))) {
				lastProcessedParam = PARAM_SEDEX_ID;
				filterClient.addFilter(new ParticipantIdFilter(request.getParameter(PARAM_SEDEX_ID)));
			}

			if (StringUtils.isNotBlank(request.getParameter(PARAM_MESSAGE_ID))) {
				lastProcessedParam = PARAM_MESSAGE_ID;
				filterClient.addFilter(new MessageIdFilter(request.getParameter(PARAM_MESSAGE_ID)));
			}


			if (lastProcessedParam == null) {
				String msg = "At least one of the following parameters is required: {filename, sedexId, state, from, until, messageId}";
				throw new MonitorException(msg);
			}

			return filterClient;
		} catch (IllegalArgumentException ex) {
			String msg = "Unable to parse parameter '" + lastProcessedParam + "'. Details: " + ex.getMessage();
			LOG.warn(msg);
			throw new MonitorException(msg, ex);
		}
	}

	private String toJson(Object obj, Class c) {
		GsonBuilder gsonBilder = new GsonBuilder();
		Gson gson = gsonBilder.create();

		return gson.toJson(obj, c);
	}
}