/*
 * $Id: SedexCertConfigFactory.java 314 2013-08-22 13:45:38Z metz $
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

import ch.admin.suis.msghandler.util.FileUtils;
import ch.admin.suis.msghandler.util.ISO8601Utils;
import ch.admin.suis.msghandler.util.XMLValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Represents the parts from the certificateConfiguration.xml (CertificateConfiguration-1-0.xsd) which are required for
 * signing. Used by SigningOutboxSedexCfg class.
 *
 * @author kb
 * @author $Author: metz $
 * @version $Revision: 314 $
 * @since 27.02.2013
 */
public class SedexCertConfigFactory {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SedexCertConfigFactory.class.
			getName());

	private SedexCertConfig sedexCertConfig = null;

	/**
	 * Factory constructor.
	 *
	 * @param configFile File object referencing the file certificateConfiguration.xml rom the sedex configuratioon.
	 * @throws ConfigurationException if the file doen't exist or if doesn't conform to the required XML Schema.
	 */
	public SedexCertConfigFactory(File configFile) throws ConfigurationException {
		FileUtils.isFile(configFile, ".certificateConfigFile[@filePath]");

		XMLConfiguration xmlConfig = new XMLConfiguration();
		// disable the schema validation by XMLConfiguration, since the config file does not contain a schema location
		xmlConfig.setSchemaValidation(false);
		xmlConfig.setFile(configFile);
		try {
			XMLValidator.validateSedexCertificateConfig(configFile);
			xmlConfig.load();
		} catch (ConfigurationException ex) {
			LOG.error("XML File: " + configFile.getAbsolutePath()
					+ " could not be loaded. It seems the XML file is not valid");
			throw ex;
		}
		parseFile(xmlConfig, configFile.getAbsolutePath());
	}

	/**
	 * When given the value of the <location/> element from the certificateConfiguration.xml file, this method
	 * tries to determine the location of the file with the private key. The following algorithm is used:
	 * <p>
	 * <ol>
	 * <li>If 'loc' doesn't contain a variable, then 'loc' is used directly.</li>
	 * <li>If a environment variable with the same name as the variable used in 'loc' is defined: replace the
	 * variable with the value of the environment variable.</li>
	 * <li>Otherwise: Replace the variable with the directory a level above the directry where certificateConfiguration.xml
	 * is located.</li>
	 * </ol>
	 *
	 * @param loc            value of the <location/> element from the certificateConfiguration.xml file
	 * @param configFileName the absolute filename of the certificateConfiguration.xml file
	 * @return A File object referencing the PKCS#12 file with the private key. My be null, if the location can't
	 * be determined.
	 */
	private File determinePrivateKeyLocation(String loc, String configFileName) {
		Pattern p = Pattern.compile(".*\\$\\{([A-Z_]*)}.*");
		Matcher m = p.matcher(loc);
		if (m.matches()) {
			// the configuration contains a variable. We try to replace it.
			// 1. try to replace it using the environment
			String name = m.group(1);
			String value = System.getenv(name);
			if (null != value) {
				return new File(loc.replace("${" + name + "}", value));
			}
			// 2. try to replace it by the directory above "conf/certificateConfiguration.xml"
			File parent1 = new File(configFileName).getParentFile();
			if (null != parent1) {
				File parent2 = parent1.getParentFile();
				return new File(loc.replace("${" + name + "}", parent2.getAbsolutePath()));
			}
			// the parent of the parent is not there: can't determine a reasonable replacement
			return null;
		}
		// Doesn't seem to contain a variable: use the value of 'loc'
		return new File(loc);
	}


	/**
	 * Parses the certificateConfiguration.xml file and collects the required configuration values.
	 *
	 * @param xmlConfig  the config file in internal representation
	 * @param configFile absolute path of the config file to be parsed
	 * @throws ConfigurationException if a configuration error is detected.
	 */
	private void parseFile(XMLConfiguration xmlConfig, final String configFile) throws ConfigurationException {

		List<SedexCertConfig> sedexCfgs = new ArrayList<>();

		List privateCertificates = xmlConfig.configurationsAt("privateCertificate");

		LOG.debug("Parsing file: " + configFile);
		for (Iterator i = privateCertificates.iterator(); i.hasNext(); ) {
			sedexCfgs.addAll(handlePrivateCertificate(i, configFile));
		}

		if (sedexCfgs.isEmpty()) {
			throw new ConfigurationException("No certificateConfiguration/privateCertificate tag found in config: "
					+ configFile);
		}

		//Falls mehrere Sedex Config Dateien vorhanden sein sollten, dann muss jeder Eintrag ein expire Date haben!
		if (sedexCfgs.size() > 1) {
			handleMultipleCerts(sedexCfgs, configFile);
		} else {
			sedexCertConfig = sedexCfgs.get(0);
		}
	}

	/**
	 * Gets the best certificate in the list
	 *
	 * @param sedexCfgs  The list of sedex certs.
	 * @param configFile The config file to match
	 * @throws ConfigurationException Badly written config file
	 */
	private void handleMultipleCerts(List<SedexCertConfig> sedexCfgs, String configFile) throws ConfigurationException {
		for (SedexCertConfig sedexConfig : sedexCfgs) {
			if (sedexConfig.getExpireDate() == null) {
				throw new ConfigurationException(
						"multiple privateCertificate elements found, but no privateCertificate/optionalInfo/expirydate. "
								+ "Can't determine expiry date of certificate. File: " + configFile);
			}

			if (sedexCertConfig == null) {
				sedexCertConfig = sedexConfig;
			} else {
				if (sedexCertConfig.getExpireDate().before(sedexConfig.getExpireDate())) {
					sedexCertConfig = sedexConfig;
				}
			}

		}
	}

	/**
	 * Method used to handle a private certificate given by {@link #parseFile(XMLConfiguration, String)}
	 *
	 * @param i          The iterator
	 * @param configFile The config file
	 * @return A list of certificates
	 * @throws ConfigurationException A config error.
	 */
	private List<SedexCertConfig> handlePrivateCertificate(Iterator i, String configFile) throws ConfigurationException {
		List<SedexCertConfig> sedexCfgs = new ArrayList<>();
		HierarchicalConfiguration sub = (HierarchicalConfiguration) i.next();

		File p12File = determinePrivateKeyLocation(sub.getString("location"), configFile);
		if (null == p12File) {
			throw new ConfigurationException(
					"The referenced file " + configFile + " contains filenames with variables!\n"
							+ "(probably ${ADAPTER_HOME} of ${SEDEX_HOME})! Define a environment variable with that name."
							+ "The variable must point to the directory, where the sedex adpater is installed.");
		}
		if (!p12File.exists()) {
			throw new ConfigurationException(
					"Maybe incorrect configuration in file: " + configFile
							+ ", specified p12 file not found: " + p12File.getAbsolutePath());
		}

		String password = sub.getString("password");
		Date expireDate = null;

		List optionalInfo = sub.configurationsAt(".optionalInfo");
		if (optionalInfo.size() == 1) {
			HierarchicalConfiguration optionalSub = (HierarchicalConfiguration) optionalInfo.get(0);
			String sExpiryDate = optionalSub.getString("expirydate");
			try {
				expireDate = ISO8601Utils.parse(sExpiryDate);
			} catch (IllegalArgumentException ex) {
				throw new ConfigurationException("Unable to parse date: " + sExpiryDate, ex);
			}
		}

		SedexCertConfig sedexConfig = new SedexCertConfig(p12File, password, expireDate);
		LOG.debug("Found: " + sedexConfig.toString());
		sedexCfgs.add(sedexConfig);

		return sedexCfgs;
	}

	public SedexCertConfig getSedexCertConfig() {
		return sedexCertConfig;
	}
}