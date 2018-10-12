package ch.admin.suis.msghandler.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Properties;

import static org.apache.log4j.Logger.getLogger;

/**
 * This is a static utility class to work with versions from the Maven POM.
 *
 * @author $Author$
 * @version $Revision$
 */
public final class PomUtils
{
  private static final Logger LOG = getLogger(PomUtils.class.getName());

  private static final String POM_PROPERTIES = "/META-INF/maven/ch.admin.msghandler/open-egov-msghandler/pom"
      + ".properties";

  private PomUtils()
  {
  }

  public static Optional<String> findProductNameWithVersionFromMavenPomProperties()
  {
    return Optional.ofNullable(findProductNameWithVersionInPath(POM_PROPERTIES));
  }

  static String findProductNameWithVersionInPath(String path)
  {
    Properties properties = new Properties();

    try
    {
      properties.load(new InputStreamReader(PomUtils.class.getResourceAsStream(path), "UTF-8"));
      return String.format("%s-%s",
          properties.getProperty("artifactId"),
          properties.getProperty("version")
      );
    } catch (NullPointerException e)
    {
      LOG.warn("the path does not exist: " + path);
      return null;
    } catch (IOException e)
    {
      LOG.error("error while reading from the pom.properties", e);
      return null;
    }
  }
}
