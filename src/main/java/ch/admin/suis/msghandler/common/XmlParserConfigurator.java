/*
 * Copyright (C) 2019 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
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
 */
package ch.admin.suis.msghandler.common;

import java.util.AbstractMap.SimpleEntry;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.digester3.Digester;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * MSGHANDLER-80
 *
 * Configures the XML parser against XXE
 *
 * see https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md
 */
public class XmlParserConfigurator
{

  private static final Logger LOG = Logger.getLogger(XmlParserConfigurator.class.getName());

  /*
   * see https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md
   */
  private static SimpleEntry[] SAXPARSER_FEATURES = new SimpleEntry[]{
    // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all
    // XML entity attacks are prevented
    // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
    new SimpleEntry<String, Boolean>("http://apache.org/xml/features/disallow-doctype-decl", true),
    
    // If you can't completely disable DTDs, then at least do the following:
    // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
    // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
    // JDK7+ - http://xml.org/sax/features/external-general-entities
    new SimpleEntry<String, Boolean>("http://xml.org/sax/features/external-general-entities", false),
    
    // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
    // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
    // JDK7+ - http://xml.org/sax/features/external-parameter-entities
    new SimpleEntry<String, Boolean>("http://xml.org/sax/features/external-parameter-entities", false),
    
    // Disable external DTDs as well
    new SimpleEntry<String, Boolean>("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)};

  /**
   * Hardens the given SAX parser against XXE (external entity injection attack). If a problem occurs only the exception 
   * is logged.
   *
   * @param parser The SAX parser to be configured according to the best practices definded by OWASP.
   *
   */
  public static void hardenDigesterAgainstXXE(SAXParser parser)
  {
    for (SimpleEntry<String, Boolean> config : SAXPARSER_FEATURES)
    {
      setFeature(parser, config.getKey(), config.getValue());
    }
  }

  private static void setFeature(SAXParser parser, String feature, boolean value)
  {
    try
    {
      parser.setFeature(feature, value);
    } catch (SAXNotRecognizedException | SAXNotSupportedException configurationEx)
    {
      LOG.warn(String.format("The XML parser could not be hardened against XXE attacks by setting %s to %s.",
          feature, value ? "true" : "false"), configurationEx);
    }
  }

  /**
   * Hardens the underlying SAX parser against XXE (external entity injection attack) of the given digester. If a
   * problem occurs only the exception is logged.
   *
   * @param digester The Digester to be configured according to the best practices definded by OWASP.
   *
   */
  public static void hardenDigesterAgainstXXE(Digester digester)
  {
    for (SimpleEntry<String, Boolean> config : SAXPARSER_FEATURES)
    {
      setFeature(digester, config.getKey(), config.getValue());
    }

    // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
    digester.setXIncludeAware(false);
  }

  private static void setFeature(Digester digester, String feature, boolean value)
  {
    try
    {
      digester.setFeature(feature, value);
    } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException configurationEx)
    {
      LOG.warn(String.format("The XML digesters parser could not be hardened against XXE attacks by setting %s to %s.",
          feature, value ? "true" : "false"), configurationEx);
    }
  }
}
