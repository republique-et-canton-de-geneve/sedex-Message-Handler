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
package ch.admin.suis.msghandler.sender;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.admin.suis.msghandler.common.Message;
import ch.admin.suis.msghandler.common.MessageHandlerContext;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.config.ClientConfiguration;
import ch.admin.suis.msghandler.config.Outbox;
import ch.admin.suis.msghandler.log.LogService;
import ch.admin.suis.msghandler.naming.NamingService;
import ch.admin.suis.msghandler.protocol.ProtocolService;
import ch.admin.suis.msghandler.sedex.SedexAdapterConfiguration;

/**
 * The unit test for the <code>SenderSessionImpl</code> class.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class SenderSessionImplTest {

	private MessageHandlerContext context;
	private ClientConfiguration configuration = new ClientConfiguration();

	// the mocks
	private LogService logService = Mockito.mock(LogService.class);
	private ProtocolService protocolService = Mockito.mock(ProtocolService.class);

	@Before
	public void setUp() throws Exception {
		context = new MessageHandlerContext();

		context.setClientConfiguration(configuration);
		context.setLogService(logService);
		context.setProtocolService(protocolService);
	}

	@After
	public void tearDown() throws Exception {
		context = null;
	}

	/**
	 * Test method for
	 * 'ch.admin.suis.msghandler.sender.SenderSessionImpl.createMessages()'
	 * 
	 * @throws ConfigurationException
	 */
	@Test
	public void testCreateMessages() throws ConfigurationException {
		String javaIoTmpdir = SystemUtils.JAVA_IO_TMPDIR + "/junit";
		File asFile = new File(javaIoTmpdir);
		asFile.mkdirs();
		configuration.setSedexAdapterConfiguration(new SedexAdapterConfiguration(
				"1-1-1", javaIoTmpdir, javaIoTmpdir,
				javaIoTmpdir, javaIoTmpdir));
		configuration.setWorkingDir(javaIoTmpdir);
		List<Outbox> outboxes = new ArrayList<>();
		outboxes.add(
				new Outbox(asFile, "1-2-3", new MessageType(2),
						NamingService.VOID));
		SenderSessionImpl impl = new SenderSessionImpl(context, outboxes);

		Collection<Message> emptyList = impl.createMessages();
		assertTrue(emptyList.isEmpty());
	}

}
