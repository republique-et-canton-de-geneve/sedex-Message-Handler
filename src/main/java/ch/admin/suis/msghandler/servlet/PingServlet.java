/*
 * $Id: PingServlet.java 327 2014-01-27 13:07:13Z blaser $
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

import ch.admin.suis.msghandler.common.MessageHandlerService;
import ch.admin.suis.msghandler.util.HtmlUtils;
import ch.admin.suis.msghandler.util.MxBeanStats;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Ping Servlet. Provides health information about the application and the JVM.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 20.07.2012
 */
public class PingServlet extends HttpServlet implements MediaType {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PingServlet.class.getName());

	private static final String PARAM_TYPE = "type";

	private static final String TYPE_MINIMAL = "minimal";

	private static final String TYPE_HEAP_SPACE = "heapSpace";

	private static final String TYPE_PERM_SPACE = "permSpace";

	private static final String TYPE_HTML = "html";

	/**
	 * {@inheritDoc }
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("Get Request: " + request);
		doProcess(request, response);
	}

	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.getWriter().println(handleRequest(request, response));

		} catch (InvalidParameterException ex) {
			LOG.error("Invalid parameter: " + ex);
			response.getWriter().println(ex.getMessage());
			response.setContentType(TEXT);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		} catch (IOException ex) {
			LOG.fatal("MonitorServlet: " + ex.getMessage(), ex);
			throw ex;
		}
	}

	private String handleRequest(HttpServletRequest request, HttpServletResponse response) throws InvalidParameterException {

		response.setContentType(TEXT);

		if (StringUtils.isNotBlank(request.getParameter(PARAM_TYPE))) {
			String paramValue = request.getParameter(PARAM_TYPE);
			if (paramValue.equalsIgnoreCase(TYPE_MINIMAL)) {
				return "ok";
			} else if (paramValue.equalsIgnoreCase(TYPE_HEAP_SPACE)) {
				return calcHeapSpace();
			} else if (paramValue.equalsIgnoreCase(TYPE_PERM_SPACE)) {
				return calcPermSpace();
			} else if (paramValue.equalsIgnoreCase(TYPE_HTML)) {
				response.setContentType(HTML);
				return createFullHtml();
			} else {
				throw new InvalidParameterException(
						"Invalid parameter. Valid parameters: type={minimal,heapSpace,permSpace,html}");
			}
		}

		response.setContentType(HTML);
		return createFullHtml();
	}

	private String calcHeapSpace() {
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		long max = heap.getMax();
		long used = heap.getUsed();
		int percent = (int) Math.round(100.0 * used / max);
		return used + ":" + max + ":" + percent;
	}

	private String calcPermSpace() {
		MemoryUsage nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		long max = nonHeap.getMax();
		long used = nonHeap.getUsed();
		int percent = (int) Math.round(100.0 * used / max);
		return used + ":" + max + ":" + percent;
	}

	private String createFullHtml() {
		return "<html><body>" +
				HtmlUtils.addTag("h1", "Alive!") +
				createHtmlInfoTable() +
				"</body></html>";
	}

	private String createHtmlInfoTable() {
		StringBuilder sb = new StringBuilder(2048);
		Map<String, String> versionMap = new HashMap<>();
		versionMap.put("version", MessageHandlerService.PRODUCT_VERSION);
		sb.append(createHtmlTableEntries(MessageHandlerService.PRODUCT_NAME, versionMap));
		sb.append(createHtmlTableEntries("getHeap", MxBeanStats.getHeap()));
		sb.append(createHtmlTableEntries("getClassLoading", MxBeanStats.getClassLoading()));
		sb.append(createHtmlTableEntries("getOperatingSystem", MxBeanStats.getOperatingSystem()));
		sb.append(createHtmlTableEntries("getPermGen", MxBeanStats.getPermGen()));
		sb.append(createHtmlTableEntries("getRuntime", MxBeanStats.getRuntime()));
		sb.append(createHtmlTableEntries("getThread", MxBeanStats.getThread()));

		return HtmlUtils.addTag("table", sb.toString());
	}

	private String createHtmlTableEntries(String title, Map<String, String> map) {
		StringBuilder sbOuter = new StringBuilder(128);

		for (Map.Entry<String, String> entry : map.entrySet()) {
			String sb = HtmlUtils.addTag("td", entry.getKey()) +
					HtmlUtils.addTag("td", entry.getValue());
			sbOuter.append(HtmlUtils.addTag("tr", sb));
		}
		String htmTitle = HtmlUtils.addTag("tr", HtmlUtils.addTag("td", HtmlUtils.addTag("b", title)));
		return htmTitle + sbOuter.toString();
	}
}