/*
 * $Id: ClientConfigurationFactoryTest.java 340 2015-08-16 14:51:19Z sasha $
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

import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author kb
 * @author $Author: sasha $
 * @version $Revision: 340 $
 * @since 07.08.2013
 */
public class ClientConfigurationFactoryTest extends TestCase {

    private static final String BS = "\\";

    public ClientConfigurationFactoryTest(String testName) {
        super(testName);
    }

    /**
     * Makes the test for the correct parsing of "\" backslashes. See 0006347.
     *
     * @throws ConfigurationException
     */
    public void testReadConfig() throws ConfigurationException {
        ClientConfigurationFactory config = new ClientConfigurationFactory("./src/test/resources/config-unc-test.xml");

        String uncXmlTag = config.getXmlConfig().getString("sedexAdapter.inboxDir");
        String uncXmlAttribute = config.getXmlConfig().getString("messageHandler.workingDir[@dirPath]");

        assertEquals(BS + BS + "unc1" + BS + "path" + BS + "tag", uncXmlTag);
        assertEquals(BS + BS + "unc2" + BS + "path" + BS + "attribute", uncXmlAttribute);
    }
}
