package ch.admin.suis.msghandler.config;

import org.junit.Test;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JsonConverterTest
{

  @Test
  public void testAsMap() throws Exception
  {
    final Map<String, Object> result = JsonConverter.asMap(
        new InputStreamReader(getClass().getResourceAsStream("/communication-confirmation.json"), "UTF-8"));

    assertEquals(result.get("submission_type"), "1");
    assertTrue(result.get("attachments") instanceof List);
  }

  @Test
  public void testAsMapWithNoJson() throws Exception
  {
    final Map<String, Object> result = JsonConverter.asMap(new StringReader("hello world"));
    assertNull(result);
  }

}


