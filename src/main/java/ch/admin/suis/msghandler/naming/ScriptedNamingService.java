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
package ch.admin.suis.msghandler.naming;

import ch.admin.suis.msghandler.config.GroovyScriptWrapper;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * The
 * <code>ScriptedNamingService</code> resolves the participant IDs with the help of some custom-defined scripting.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public final class ScriptedNamingService implements NamingService {

  private final GroovyScriptWrapper groovyScriptWrapper;

  private final String method;
  private final File path;

  public ScriptedNamingService(File path, String method){
    this.groovyScriptWrapper = new GroovyScriptWrapper(path);
    this.method = method;
    this.path = path;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public String resolve(Object filename) {
    // for the moment we just show if there are the IDs known to us
    if(null == filename || StringUtils.isBlank(filename.toString())) {
      return null;
    }

    return groovyScriptWrapper.callMethodReturningString(method, filename);
  }

  @Override
  public String toString(){
    return "ScriptedNamingService: Path: " + path.getAbsolutePath() + ", Method: " + method;
  }
}
