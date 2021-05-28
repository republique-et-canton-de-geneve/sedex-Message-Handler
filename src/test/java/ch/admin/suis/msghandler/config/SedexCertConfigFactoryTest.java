/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.admin.suis.msghandler.config;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 *
 * @author kb
 */
public class SedexCertConfigFactoryTest {

	@Test
	public void testReadConfig() throws ConfigurationException {
		SedexCertConfigFactory facotry = new SedexCertConfigFactory(new File(
				"./src/test/resources/complete/mh/install-dir/conf/certificateConfiguration.xml"));
		SedexCertConfig sedexCertConfig = facotry.getSedexCertConfig();
		assertEquals("Wrong privateCertificate selected", "12345678",
				sedexCertConfig.getPassword());
	}

	@Test
	public void testReadConfigOne() throws ConfigurationException {
		SedexCertConfigFactory facotry = new SedexCertConfigFactory(
				new File("./src/test/resources/sedex-cert-config/oldConfigOne.xml"));
		SedexCertConfig sedexCertConfig = facotry.getSedexCertConfig();
		assertEquals("Wrong privateCertificate selected", "dummyPassword",
				sedexCertConfig.getPassword());
	}

	@Test
	public void testReadConfigWithAddedElement() throws ConfigurationException {
		SedexCertConfigFactory facotry = new SedexCertConfigFactory(new File(
				"./src/test/resources/sedex-cert-config/changedConfigOne.xml"));
		SedexCertConfig sedexCertConfig = facotry.getSedexCertConfig();
		assertEquals("Wrong privateCertificate selected",
				"theOneConfigWithAdditionalElement", sedexCertConfig.getPassword());
	}

	@Test
	public void testReadConfigWithExpiryElement() throws ConfigurationException {
		SedexCertConfigFactory facotry = new SedexCertConfigFactory(new File(
				"./src/test/resources/sedex-cert-config/oldConfigTwoWithExpiry.xml"));
		SedexCertConfig sedexCertConfig = facotry.getSedexCertConfig();
		assertEquals("Wrong privateCertificate selected",
				"that-expires-in-distant-future", sedexCertConfig.getPassword());
	}

	@Test(expected = ConfigurationException.class)
	public void testSchemaInvalid() throws ConfigurationException {
		// we do not validate against XSD anymore, check that it behaves well
		new SedexCertConfigFactory(
				new File("./src/test/resources/sedex-cert-config/schema_invalid.xml"));
	}

	@Test(expected = ConfigurationException.class)
	public void testNotWellFormed() throws ConfigurationException {
		// we do not validate against XSD anymore, check that it behaves well
		new SedexCertConfigFactory(
				new File("./src/test/resources/sedex-cert-config/malformed.xml"));
	}

	@Test(expected = ConfigurationException.class)
	public void testTwoCertsNoExpiryDates() throws ConfigurationException {
		// we do not validate against XSD anymore, check that it behaves well
		new SedexCertConfigFactory(new File(
				"./src/test/resources/sedex-cert-config/oldConfigTwoNoExpiryDates.xml"));
	}

	@Test(expected = ConfigurationException.class)
	public void testNoPrivCertElement() throws ConfigurationException {
		// we do not validate against XSD anymore, check that it behaves well
		new SedexCertConfigFactory(new File(
				"./src/test/resources/sedex-cert-config/schema_invalid_no_priv_cert.xml"));
	}
}
