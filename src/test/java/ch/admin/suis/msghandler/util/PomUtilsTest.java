package ch.admin.suis.msghandler.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PomUtilsTest
{

  @Test
  public void findProductNameWithVersionInPath()
  {
    assertEquals("open-egov-msghandler-0.0.0",
        PomUtils.findProductNameWithVersionInPath("/utils/pom_testing.properties"));
  }

  @Test
  public void findProductNameWithVersionInPathOnNonExistingPath()
  {
    assertNull(PomUtils.findProductNameWithVersionInPath("/non_existing.properties"));
  }

}
