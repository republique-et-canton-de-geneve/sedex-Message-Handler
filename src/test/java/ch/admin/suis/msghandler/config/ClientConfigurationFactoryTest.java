/*
 * $Id$
 *
 * Copyright 2013 by Swiss Federal Administration
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of the Swiss Federal Administration. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with the Swiss Federal Administration.
 */
package ch.admin.suis.msghandler.config;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 07.08.2013
 */
public class ClientConfigurationFactoryTest
{

  private static final String BS = "\\";


  /**
   * Makes the test for the correct parsing of "\" backslashes. See 0006347.
   * @throws ConfigurationException
   */
	@Test
  public void testReadConfig() throws ConfigurationException
  {
    ClientConfigurationFactory config = new ClientConfigurationFactory("./src/test/resources/config-unc-test.xml");

    String uncXmlTag = config.getXmlConfig().getString("sedexAdapter.inboxDir");
    String uncXmlAttribute = config.getXmlConfig().getString("messageHandler.workingDir[@dirPath]");

    assertEquals(BS + BS + "unc1" + BS + "path" + BS + "tag", uncXmlTag);
    assertEquals(BS + BS + "unc2" + BS + "path" + BS + "attribute", uncXmlAttribute);
  }
}
