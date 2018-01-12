/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.admin.suis.msghandler.config;

import java.io.File;

import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author kb
 */
public class SedexCertConfigFactoryTest extends TestCase {

    public SedexCertConfigFactoryTest(String testName) {
        super(testName);
    }

    public void testReadConfig() throws ConfigurationException {
        SedexCertConfigFactory facotry = new SedexCertConfigFactory(new File(
                "./src/test/resources/complete/mh/install-dir/conf/certificateConfiguration.xml"));
        SedexCertConfig sedexCertConfig = facotry.getSedexCertConfig();
        assertEquals("Wrong privateCertificate selected", "12345678", sedexCertConfig.getPassword());
    }
}
