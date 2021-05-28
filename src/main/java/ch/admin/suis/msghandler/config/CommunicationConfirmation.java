package ch.admin.suis.msghandler.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Wrapper over the communication confirmation in the given container file.
 *
 * @author $Author$
 * @version $Revision$
 */

public class CommunicationConfirmation
{
  public static final String COMMUNICATION_CONFIRMATION_JSON = "communication-confirmation.json";

  private final File container;

  public CommunicationConfirmation(final File container)
  {
    this.container = container;
  }

  /**
   * Extracts the confirmation and returns it as string.
   *
   * @throws IOException
   */
  public Map<String, Object> extract() throws IOException
  {

    try (ZipFile zipFile = new ZipFile(container))
    {
      final ZipEntry zipEntry = zipFile.getEntry(COMMUNICATION_CONFIRMATION_JSON);
      if (null != zipEntry)
      {
        return JsonConverter.asMap(new InputStreamReader(zipFile.getInputStream(zipEntry), "UTF-8"));
      }
    }

    return null;
  }
}
