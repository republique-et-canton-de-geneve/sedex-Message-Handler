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
 * $Id: SenderSessionImplTest.java 327 2014-01-27 13:07:13Z blaser $
 */
package ch.admin.suis.msghandler.sender;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import ch.admin.suis.msghandler.config.ClientConfiguration;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.protocol.ProtocolService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The unit test for the <code>SenderSessionImpl</code> class.
 *
 * @author Alexander Nikiforov
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public class SenderSessionImplTest extends TestCase {

    private MessageHandlerContext context;
    private ClientConfiguration configuration = new ClientConfiguration();

    // the mocks
    private LogService logService = EasyMock.createStrictMock(LogService.class);
    private ProtocolService protocolService = EasyMock.createStrictMock(ProtocolService.class);

    public SenderSessionImplTest(String testName) {
        super(testName);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        context = new MessageHandlerContext();

        context.setClientConfiguration(configuration);
        context.setLogService(logService);
        context.setProtocolService(protocolService);
    }

    /*
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        context = null;
        super.tearDown();
    }

    /**
     * Test method for 'ch.admin.suis.msghandler.sender.SenderSessionImpl.createMessages()'
     */
    public void testCreateMessages() {
        // **************** the fixture

    }

    /*
     * Test method for 'ch.admin.suis.msghandler.sender.SenderSessionImpl.send(Message)'
     */
    public void testSend() {

    }

    /*
     * Test method for 'ch.admin.suis.msghandler.sender.SenderSessionImpl.commit(Message)'
     */
    public void testCommit() {

    }

    /*
     * Test method for 'ch.admin.suis.msghandler.sender.SenderSessionImpl.logSuccess(Message)'
     */
    public void testLogSuccess() {

    }

    /*
     * Test method for 'ch.admin.suis.msghandler.sender.SenderSessionImpl.logError(Message, Exception)'
     */
    public void testLogError() {

    }

    /*
     * Custom test in order to check the delay between the arrival of the document and the posting itself.
     */
    public void testWaitForDocument() {

    }

}
