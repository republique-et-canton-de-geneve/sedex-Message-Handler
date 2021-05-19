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
package ch.admin.suis.msghandler.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BaseServlet. Just a help text if the URL is not correct.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 20.07.2012
 */
public class BaseServlet extends HttpServlet implements MediaType {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BaseServlet.class.getName());

  /**
   * {@inheritDoc }
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    LOG.debug("Get Request: " + request);
    doProcess(request, response);
  }

  private void doProcess(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try{
      String helpTxt = "Nothing todo. Add one of these pathes: {monitor, ping, trigger}";
      response.getWriter().println(helpTxt);
    }
    catch(IOException ex){
      LOG.fatal("MonitorServlet: " + ex.getMessage(), ex);
      throw ex;
    }
  }
}