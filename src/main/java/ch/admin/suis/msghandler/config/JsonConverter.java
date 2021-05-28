package ch.admin.suis.msghandler.config;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

import static org.apache.log4j.Logger.getLogger;

/**
 * Static utility class to read JSON from input stream
 *
 * @author $Author$
 * @version $Revision$
 */

public class JsonConverter
{
  private static final Logger LOG = getLogger(JsonConverter.class.getName());

  /**
   * Reads the given input stream and returns a map representation.
   *
   * @param jsonContent
   *     input stream with JSON
   * @return map of Strings or null, if the content cannot be properly read
   * @throws IOException
   *     if the input stream cannot be read
   */
  public static Map<String, Object> asMap(final Reader jsonContent) throws IOException
  {
    final Gson gson = new Gson();
    final Type type = new TypeToken<Map<String, Object>>()
    {
    }.getType();

    try
    {
      return gson.fromJson(new JsonReader(jsonContent), type);
    } catch (JsonParseException e)
    {
      LOG.fatal("cannot read the provided JSON content", e);
    }

    return null;
  }
}
