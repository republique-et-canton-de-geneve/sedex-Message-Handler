/*
 * $Id: ScriptedNamingService.java 327 2014-01-27 13:07:13Z blaser $
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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * The
 * <code>ScriptedNamingService</code> resolves the participant IDs with the help of some custom-defined scripting.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public final class ScriptedNamingService implements NamingService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ScriptedNamingService.class.
			getName());

	private final File path;

	private final String method;

	public ScriptedNamingService(File path, String method) {
		this.path = path;
		this.method = method;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String resolve(Object filename) {
		// for the moment we just show if there are the IDs known to us
		if (null == filename || StringUtils.isBlank(filename.toString())) {
			return null;
		}

		return resolveInGroovy(filename);
	}

	private String resolveInGroovy(Object filename) {
		try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())) {

			@SuppressWarnings("rawtypes")
			Class groovyClass = loader.parseClass(path);

			// call the function out of it
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

			return (String) groovyObject.invokeMethod(method, new Object[]{filename});
		} catch (CompilationFailedException e) {
			// the engine cannot compile the source
			LOG.fatal("error while compiling the resolver script " + path.getAbsolutePath(), e);
		} catch (InstantiationException | IllegalAccessException e) {
			// the engine cannot evalualte the source
			LOG.fatal("cannot load or execute the resolver script " + path.getAbsolutePath(), e);
		} catch (IOException e) {
			LOG.fatal("cannot read the filename resolver script " + path.getAbsolutePath(), e);
		}

		return null;
	}

	@Override
	public String toString() {
		return "ScriptedNamingService: Path: " + path.getAbsolutePath() + ", Method: " + method;
	}
}
