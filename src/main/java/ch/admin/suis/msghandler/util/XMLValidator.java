/*
 * $Id: XMLValidator.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * Utility class for XML schema validations.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 26.02.2013
 */
public final class XMLValidator {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(XMLValidator.class.getName());

  private XMLValidator() {
  }

  /**
   * Validiert das sedex certificate configuration file. Dies ist mit der Klasse XMLConfiguration nicht möglich, da
   * das File keine "xsi:schemaLocation" enthält.
   *
   * @param xmlFile File object referenceing the file to be validated.
   * @throws ConfigurationException Config problems...
   */
  public static void validateSedexCertificateConfig(File xmlFile) throws ConfigurationException {
    try {
      String schemaUrl = "http://www.sedex.ch/xmlns/certificateConfiguration/1 " + XMLValidator.class.
              getResource("/conf/CertificateConfiguration-1-0.xsd").toExternalForm();

      LOG.debug("Schema location for sedex cert config: " + schemaUrl);
      validateXml(readFile(xmlFile), schemaUrl);
    } catch (SAXException | IOException ex) {
      throw new ConfigurationException("Unable to validate sedex certificate config. ex: " + ex.getMessage(), ex);
    }
  }

  /**
   * Validates eCH-0090-v1 XML.
   *
   * @param data String non validated XML data
   * @throws SAXException XML problem
   * @throws IOException  IO problem
   * @deprecated Used by other deprecated methods. Otherwise, same code.
   */
  @Deprecated
  public static void validateEch0090_1(String data) throws SAXException, IOException {
    String schemaUrl = "http://www.ech.ch/xmlns/eCH-0090/1 " + XMLValidator.class.getResource(
            "/eCH-0090-1-0.xsd").toExternalForm();

    LOG.debug("Schema location for eCH-0090-1-0.xsd: " + schemaUrl);
    validateXml(data, schemaUrl);
  }

  /**
   * Validates eCH-0090-v2 XML. Funny thing, it's actually the same structure as a v1.
   *
   * @param data String non validated XML data
   * @throws SAXException You probably have an XML formatting problem.
   * @throws IOException  No good ! Something came up
   */
  public static void validateEch0090_2(String data) throws SAXException, IOException {
    String schemaUrl = "http://www.ech.ch/xmlns/eCH-0090/2 " + XMLValidator.class.getResource(
            "/eCH-0090-2-0.xsd").toExternalForm();
    LOG.debug("Schema location for eCH-0090-2-0.xsd: " + schemaUrl);
    validateXml(data, schemaUrl);
  }

  /**
   * Validates an XML String (serialized object) against the schema. When validation fails, this method
   * will throw an exception...
   *
   * @param data      Raw XML.
   * @param schemaUrl uRL of the schema
   * @throws SAXException XML problems
   * @throws IOException  IO problems, not good
   */
  private static void validateXml(String data, String schemaUrl) throws SAXException, IOException {
    SAXParser parser = new SAXParser();
    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

    parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaUrl);

    Validator validator = new Validator();
    parser.setErrorHandler(validator);
    parser.parse(new InputSource(new StringReader(data)));
    if (validator.hasError()) {
      throw validator.getError();
    }
  }

  private static String readFile(File file) {
    try {
      try (FileInputStream inputStream = new FileInputStream(file)) {
        return IOUtils.toString(inputStream);
      }
    } catch (Exception ex) {
      throw new UnhandledException(new IOException(ex));
    }
  }

  /**
   * @author Alexander Nikiforov
   * @author $Author: blaser $
   * @version $Revision: 327 $
   */
  private static final class Validator extends DefaultHandler {

    private SAXException error;

    @Override
    public void error(SAXParseException e) throws SAXException {
      super.error(e);
      error = e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      super.fatalError(e);
      error = e;
    }

    boolean hasError() {
      return null != error;
    }

    SAXException getError() {
      return error;
    }
  }
}