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
 * $Id: MessageTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.common;

import ch.admin.suis.msghandler.sender.SenderSession;
import ch.admin.suis.msghandler.util.V2MessageXmlGenerator;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Ignore;
import org.junit.Test;


/**
 * The unit test for the <code>Message</code> class.
 * It tests the creation and serialization of the Sedex messages.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class MessageTest extends TestCase {

    protected void setUp() throws Exception {
        SenderSession.msgGen = new V2MessageXmlGenerator();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testISO8601DateTime() throws Exception {
        //DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecondMillis();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
        if (DateTimeZone.getDefault() != DateTimeZone.forID("Europe/Berlin")){
            // Abnormal behaviour
            System.out.println("Very abnormal behaviour. It seems I am not timezone configured, or at least not properly.");
            DateTimeZone.setDefault(DateTimeZone.forID("Europe/Berlin"));
        }

        assertNotNull(fmt);

        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, 53, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T15:01:58.053Z"));
        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T15:01:58Z"));

        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, 53, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T15:01:58.053+00:00"));
        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T15:01:58+00:00"));


        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, 53, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T17:01:58.053+02:00"));
        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, 53, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T17:01:58.053"));
        assertEquals(new DateTime(2012, 6, 20, 17, 01, 58, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T17:01:58"));
        assertEquals(new DateTime(2012, 6, 20, 17, 01, DateTimeZone.getDefault()),
                fmt.parseDateTime("2012-06-20T17:01"));

    }

    /**
     * Test method for 'ch.admin.suis.msghandler.common.Message.writeEnvelope(Writer)'
     */
    public void testWriteEnvelope() throws Exception {
        // create a message from an existing envelope
        // and serialize the message into a string

        // validate the written content by re-reading the message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_4.xml"));
        message.validate();

        assertNotNull(message);
        assertEquals("4", message.getMessageId());
        assertEquals(new MessageType(94), message.getMessageType());
        assertEquals("0", message.getMessageClass());
        assertEquals("T7-4-1", message.getSenderId());
        assertEquals("T7-4-2", message.getRecipientsAsString());
        assertEquals("2008-05-27T15:00:00", message.getEventDate());
        assertEquals("2008-05-28T13:00:00", message.getMessageDate());

    }

    /**
     * Test method for 'ch.admin.suis.msghandler.common.Message.createFrom(Reader)'
     */
    public void testCreateFrom() throws Exception {
        // the real message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_4.xml"));

        assertNotNull(message);
        assertEquals("4", message.getMessageId());
        assertEquals(new MessageType(94), message.getMessageType());
        assertEquals("0", message.getMessageClass());
        assertEquals("T7-4-1", message.getSenderId());
        assertEquals("T7-4-2", message.getRecipientsAsString());
        assertEquals("2008-05-27T15:00:00", message.getEventDate());
        assertEquals("2008-05-28T13:00:00", message.getMessageDate());
    }

    /**
     * Test method for 'ch.admin.suis.msghandler.common.Message.createFrom(Reader)'
     */
    public void testEnvelopeSchwitter() throws Exception {
        // the real message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_18f99837-35c8-4beb-9278-0306167fa09d.xml"));

    /*
     *   <messageId>2219-2394952B-E26B-4C45-8766-AFDC095</messageId>
  <messageType>10099</messageType>
  <messageClass>0</messageClass>
  <senderId>1-2421-1</senderId>
  <recipientId>2-SO-1</recipientId>
  <eventDate>2011-12-31T00:00:00</eventDate>
  <messageDate>2012-06-20T17:01:58.53</messageDate>
     */
        assertNotNull(message);
        assertEquals("2219-2394952B-E26B-4C45-8766-AFDC095", message.getMessageId());
        assertEquals(new MessageType(10099), message.getMessageType());
        assertEquals("0", message.getMessageClass());
        assertEquals("1-2421-1", message.getSenderId());
        assertEquals("2-SO-1", message.getRecipientsAsString());
        assertEquals("2011-12-31T00:00:00", message.getEventDate());
        assertEquals("2012-06-20T17:01:58.53", message.getMessageDate());
    }

    /**
     * Test method for 'ch.admin.suis.msghandler.common.Message.createFrom(Reader)'
     */
    public void testCreateFromOtherEnvelope() throws Exception {
        // the real message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_4c7e2c28-dfeb-4724-904d-e88dca0d9f0c.xml"));

        assertNotNull(message);
        assertEquals("3-CH-1", message.getRecipientsAsString());
        assertEquals("1-2511-1", message.getSenderId());
    }


    /**
     * Tests the message validation.
     *
     * @throws Exception
     */
    public void testValidate() throws Exception {
        // a real message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_4.xml"));

        // validation should be ok
        message.validate();

        // participant ID
        message.addRecipientId("");
        try {
            message.validate();
            fail("the message is validated but must have been not");
        } catch (Exception e) {
            // ok
        }

        message.addRecipientId("500019090");
        try {
            message.validate();
            fail("the message is validated but must have been not");
        } catch (Exception e) {
            // ok
        }

    }

    /**
     * Tests a message with BOM in the beginning.
     *
     * @throws Exception
     */
    public void testCreateFromAchiEnvelope() throws Exception {
        // the real message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_f285a6d4-917a-4d9b-bc03-ae64d8bf2354.xml"));

        message.validate();

        assertNotNull(message);
        assertEquals("3-CH-1", message.getRecipientsAsString());
        assertEquals("1-2511-1", message.getSenderId());
    }

    public void testMultiplreRecipients() throws Exception {
        // create a message from an existing envelope
        // and serialize the message into a string

        // validate the written content by re-reading the message
        Message message = Message.createFrom(getClass().getResourceAsStream("/xml/envl_Test8.xml"));

        message.validate();

        assertNotNull(message);
        assertEquals("Test8", message.getMessageId());
        assertEquals(new MessageType(94), message.getMessageType());
        assertEquals("0", message.getMessageClass());
        assertEquals("T7-4-2", message.getSenderId());
        assertEquals("T7-4-2, T7-4-3, T7-4-4", message.getRecipientsAsString());
        assertEquals(3, message.getRecipientIds().size());
        assertEquals("2008-05-27T15:00:00", message.getEventDate());
        assertEquals("2008-05-27T15:30:00", message.getMessageDate());
    }
}
