package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.ui.View.ADD_CONTACT;
import static de.fu_berlin.inf.dpp.ui.View.MAIN_VIEW;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddContactTest extends StfHtmlTestCase {
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void setUp() throws Exception {
    Util.removeTestersFromContactList(ALICE, BOB);
  }

  @Test
  public void addBobAsContact() throws Exception {
    // Precondition
    List<String> aliceContactList = ALICE.htmlBot().getContactList(MAIN_VIEW);
    List<String> bobContactList = BOB.htmlBot().getContactList(MAIN_VIEW);
    assertFalse("Alice still has Bob as contact", aliceContactList.contains(BOB.getBaseJid()));
    assertFalse("Bob still has Alice as contact", bobContactList.contains(ALICE.getBaseJid()));

    // open contact form
    ALICE.htmlBot().view(MAIN_VIEW).button("add-contact").click();
    assertTrue(ALICE.htmlBot().view(ADD_CONTACT).isOpen());

    // add bob as contact
    ALICE.htmlBot().view(ADD_CONTACT).inputField("jid").enter(BOB.getBaseJid());
    ALICE.htmlBot().view(ADD_CONTACT).button("add-contact").click();
    assertTrue("Main View did not open", ALICE.htmlBot().view(MAIN_VIEW).isOpen());

    // confirm
    BOB.superBot().confirmShellRequestOfSubscriptionReceived();
    ALICE.superBot().confirmShellRequestOfSubscriptionReceived();

    // check new contact
    aliceContactList = ALICE.htmlBot().getContactList(MAIN_VIEW);
    bobContactList = BOB.htmlBot().getContactList(MAIN_VIEW);
    assertTrue("Alice has Bob not as contact", aliceContactList.contains(BOB.getBaseJid()));
    assertTrue("Bob has Alice not as contact", bobContactList.contains(ALICE.getBaseJid()));
  }
}
