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
package ch.admin.suis.msghandler.util;

import ch.admin.suis.msghandler.common.XmlParserConfigurator;
import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class for XML schema validations.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 26.02.2013
 */
public final class XMLValidator {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(XMLValidator.class.getName());

  private XMLValidator() {
  }

  public static void validateEch0090_1(String data) throws SAXException, IOException {
    String schemaUrl = "http://www.ech.ch/xmlns/eCH-0090/1 " + new XMLValidator().getClass().getResource(
            "/eCH-0090-1-0.xsd").toExternalForm();

    LOG.debug("Schema location for eCH-0090-1-0.xsd: " + schemaUrl);
    validateXml(data, schemaUrl);
  }

  /**
   * Validates an XML String (serialized object) against the schema. When validation fails, this method
   * will throw an exception...
   *
   * @param data
   * @throws SAXException
   * @throws IOException
   */
  public static void validateXml(String data, String schemaUrl) throws SAXException, IOException {
    SAXParser parser = new SAXParser();
    XmlParserConfigurator.hardenDigesterAgainstXXE(parser);
    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

    parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaUrl);

    Validator validator = new Validator();
    parser.setErrorHandler(validator);
    parser.parse(new InputSource(new StringReader(data)));
    if(validator.hasError()) {
      throw validator.getError();
    }
  }

  /**
   *
   * @author Alexander Nikiforov
   * @author $Author$
   * @version $Revision$
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