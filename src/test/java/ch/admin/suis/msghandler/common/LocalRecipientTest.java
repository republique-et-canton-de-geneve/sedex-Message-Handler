/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.admin.suis.msghandler.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 * @author kb
 */
public class LocalRecipientTest {

	@Test
  public void testSomeMethod() {
    LocalRecipient lr = new LocalRecipient("T1-11-1", "11 22 33");
    assertTrue(lr.containsMsgType(11));
    assertTrue(lr.containsMsgType(22));
    assertTrue(lr.containsMsgType(33));
    assertFalse(lr.containsMsgType(44));

  }
}
