package ch.admin.suis.msghandler.config;

import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * Class to call the Groovy scripts.
 *
 * @author $Author$
 * @version $Revision$
 */

public class GroovyScriptWrapper
{
  private static final Logger LOG = getLogger(GroovyScriptWrapper.class.getName());

  private final File path;

  /**
   * Creates a new object
   *
   * @param path
   *     absolute path to the script
   */
  public GroovyScriptWrapper(final File path)
  {
    this.path = path;
  }

  /**
   * Calls the given method with the provided parameters. The method is assumed to return a string object.
   *
   * @param parameters
   *     parameters of the call
   * @return string result of the method call or null if the call was not successful
   */
  public String callMethodReturningString(final String method, Object... parameters)
  {
		try (GroovyClassLoader loader = new GroovyClassLoader(
				getClass().getClassLoader())) {

      @SuppressWarnings("rawtypes")
      Class groovyClass = loader.parseClass(path);

      // call the function out of it
      GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

      return (String) groovyObject.invokeMethod(method, parameters);
    } catch (CompilationFailedException e)
    {
      // the engine cannot compile the source
      LOG.fatal("error while compiling the resolver script " + path.getAbsolutePath(), e);
    } catch (InstantiationException | IllegalAccessException e)
    {
      // the engine cannot evalualte the source
      LOG.fatal("cannot load or execute the resolver script " + path.getAbsolutePath(), e);
    } catch (IOException e)
    {
      LOG.fatal("cannot read the filename resolver script " + path.getAbsolutePath(), e);
    }

    return null;

  }

}
