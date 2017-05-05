/*
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
 * $Id: ClientConfigurationFactoryTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.config.ClientConfigurationFactory;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Unit test for the
 * <code>ClientConfigurationFactory</code> class.
 * <p>
 * This is like an integration test. Somewhere should be a document which describes which files have to be on the right
 * place. Document name: "completeTest.txt". Use the linux "tree" command on the directory
 * "src/test/resources/complete". This may help you to understand...
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class ClientConfigurationFactoryTest extends TestCase {

    //  public void testReferenceConfig() throws Exception {
//    ClientConfigurationFactory.create("newConfig-byKB.xml");
//  }
    public void testCreateConfig1() throws ConfigurationException {
        // Just test the configuration
        new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config.xml").init();

        try {
            new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config-doublication1.xml").init();
            fail();
        } catch (ConfigurationException ex) {
            assertTrue(ex.getMessage().contains("XML doublication"));
        }

        try {
            new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config-invalidpath.xml").init();
            fail();
        } catch (ConfigurationException ex) {
            assertTrue(ex.getMessage().contains("signingOutbox1_1_NOT_EXIST"));
        }
    }

    /**
     * Test following case: There's a normal sender available. Application like eSchKG or similar. In this case a
     * participantIdResolver has to be present.
     */
    public void testCreateConfig2() {
        try {
            new ClientConfigurationFactory(
                    "./src/test/resources/complete/mh/install-dir/conf/config-invalidMissingIdResolver.xml").init();
            fail();
        } catch (ConfigurationException ex) {
            assertTrue(ex.getMessage().contains("participiantResolverXYZ.groovy either not exist or is not a file"));
        }
    }

    public void testSedexCertConfig() throws ConfigurationException {
        new ClientConfigurationFactory(
                "./src/test/resources/complete/mh/install-dir/conf/configSedexCert.xml").init();
    }
}
