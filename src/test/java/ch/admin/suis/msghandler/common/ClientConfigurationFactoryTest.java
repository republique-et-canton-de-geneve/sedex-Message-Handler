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
 * $Id$
 */
package ch.admin.suis.msghandler.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.admin.suis.msghandler.config.ClientConfigurationFactory;

/**
 * Unit test for the
 * <code>ClientConfigurationFactory</code> class.
 *
 * This is like an integration test. Somewhere should be a document which describes which files have to be on the right
 * place. Document name: "completeTest.txt". Use the linux "tree" command on the directory
 * "src/test/resources/complete". This may help you to understand...
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */

public class ClientConfigurationFactoryTest {

  private static final String BASE_PATH_SEDEX = "./src/test/resources/complete/sedex";
  private static final String BASE_PATH_MH = "./src/test/resources/complete/mh//base-path";

  private static final List<File> TEMP_DIRS = Arrays.asList(
      new File("./src/test/resources/complete/DB"),
      new File(BASE_PATH_SEDEX + "/inbox"),
      new File(BASE_PATH_SEDEX + "/outbox"),
      new File(BASE_PATH_SEDEX + "/receipts"),
      new File(BASE_PATH_SEDEX + "/sent"),
      new File(BASE_PATH_MH + "/sent"),
      new File(BASE_PATH_MH + "/corrupted"),
      new File(BASE_PATH_MH + "/unknown"),
      new File(BASE_PATH_MH + "/tmp/preparing"),
      new File(BASE_PATH_MH + "/tmp/receiving"),
      new File(BASE_PATH_MH + "/inbox1"),
      new File(BASE_PATH_MH + "/outbox1"),
      new File(BASE_PATH_MH + "/inbox2"),
      new File(BASE_PATH_MH + "/outbox2"),
      new File(BASE_PATH_MH + "/inbox3"),
      new File(BASE_PATH_MH + "/outbox3"),
      new File(BASE_PATH_MH + "/inbox4"),
      new File(BASE_PATH_MH + "/inbox5"),
      new File(BASE_PATH_MH + "/inbox2a"),
      new File(BASE_PATH_MH + "/outbox2a"),
      new File(BASE_PATH_MH + "/receipts"),
      new File(BASE_PATH_MH + "/inboxTransparent"),
      new File(BASE_PATH_MH + "/outboxTransparent"),
      new File(BASE_PATH_MH + "/outboxTransparent2"),

      new File(BASE_PATH_MH + "/signingOutbox2"),
      new File(BASE_PATH_MH + "/signingOutbox2Processed"),
      new File(BASE_PATH_MH + "/signingOutbox1_1"),
      new File(BASE_PATH_MH + "/signingOutbox1_2")
  );



	@Before
  public void setUp() throws Exception
	{
    TEMP_DIRS.forEach((dir) ->
        {
          try
          {
            FileUtils.forceMkdir(dir);
          } catch (IOException ex)
          {
            // ignore
          }
    });
  }

	@After
	public void tearDown() throws Exception
  {
    // Lösche die für die Tests erforderlichen Verzeichnisse rekursiv
    TEMP_DIRS.forEach((dir) ->
        {
          try
          {
            FileUtils.deleteDirectory(dir);
          } catch (IOException ex)
          {
            // ignore
          }
    });

  }

	@Test
  public void testCreateConfig1() throws ConfigurationException {
    // Just test the configuration
    new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config.xml").init();

    try{
      new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config-doublication1.xml").init();
      fail();
    }
    catch(ConfigurationException ex){
      assertTrue(ex.getMessage().contains("XML doublication"));
    }

    try{
      new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/config-invalidpath.xml").init();
      fail();
    }
    catch(ConfigurationException ex){
      assertTrue(ex.getMessage().contains("signingOutbox1_1_NOT_EXIST"));
    }
  }

  /**
   * Test following case: There's a normal sender available. Application like eSchKG or similar. In this case a
   * participantIdResolver has to be present.
   */
	@Test
  public void testCreateConfig2() {
    try{
      new ClientConfigurationFactory(
              "./src/test/resources/complete/mh/install-dir/conf/config-invalidMissingIdResolver.xml").init();
      fail();
    }
    catch(ConfigurationException ex){
      assertTrue(ex.getMessage().contains("participiantResolverXYZ.groovy either not exist or is not a file"));
    }
  }

	@Test
  public void testSedexCertConfig() throws ConfigurationException {
    new ClientConfigurationFactory("./src/test/resources/complete/mh/install-dir/conf/configSedexCert.xml").init();
  }
}
