/*
 * Copyright 2019 by Swiss Federal Administration
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of the Swiss Federal Administration. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with the Swiss Federal Administration.
 */
package ch.admin.suis.msghandler.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Only tests validateEch0090_1, since it interally invokes the method validateXml.
 *
 * @author Adrian Greiler
 */
public class XMLValidatorTest
{
  
  public XMLValidatorTest()
  {
  }

  /**
   * Test of validateEch0090_1 method, of class XMLValidator.
   */
  @Test
  public void testValidateEch0090_1() throws Exception
  {
    String data =
        new String(Files.readAllBytes(Paths.get("src/test/resources/xml/envl_Test7.xml")), StandardCharsets.UTF_8);
    XMLValidator.validateEch0090_1(data);
  }

  @Test(expected = org.xml.sax.SAXParseException.class)
  public void testValidateXML_XXE() throws Exception
  {
    String data =
        new String(Files.readAllBytes(Paths.get("src/test/resources/xml/envl_Test_XXE.xml")), StandardCharsets.UTF_8);
    XMLValidator.validateEch0090_1(data);

    fail("An external dtd should not be loaded: src/test/resources/xml/envl_Test_XXE.xml");
  }
}
