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
 * $Id: ReceiptTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.sender.SenderSession;
import ch.admin.suis.msghandler.util.V2MessageXmlGenerator;
import ch.admin.suis.msghandler.util.V2ReceiptXmlGenerator;
import ch.admin.suis.msghandler.util.XMLGenerator;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Unit test for the <code>Receipt</code> class.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class ReceiptTest extends TestCase {

    protected void setUp() throws Exception {
        SenderSession.msgGen = new V2MessageXmlGenerator();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for 'ch.admin.suis.msghandler.common.Receipt.createFrom(Reader)'
     */
    public void testCreateFrom() throws Exception {
        Receipt receipt = Receipt.createFrom(getClass().getResourceAsStream("/xml/receipt__ID_1_0.xml"));

        assertNotNull(receipt);
    }

    /**
     * This tests whether the V2 version of the receipt is behaving correctly.
     * @throws SAXException XML problems.
     * @throws IOException IO problems, generally a bad sign
     */
    public void testV2ReceiptHandling() throws SAXException, JAXBException, IOException, ParseException,
            DatatypeConfigurationException {

        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_4-v2.xml"));
        V2ReceiptXmlGenerator gen = new V2ReceiptXmlGenerator();

        assertNotNull(gen.generateSuccess(message, "T9-GE-1494"));
        message.setSenderId("THIS SHOULD MAKE YOU CRASH");
        try{
            gen.generateSuccess(message, "T9-GE-1494");
            fail("Message is considered as valid whereas it should not.");
        } catch (SAXException e){
            // Normal behaviour
        }
    }

}
