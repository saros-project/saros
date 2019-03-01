package saros.stf.test.roster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;

@TestLink(id = "Saros-117_sort_online_contacts_over_offline")
public class SortContactsOnlineOverOfflineTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, CARL, BOB);
  }

  @Test
  public void testSortContactsOnlineOverOffline() throws Exception {

    // wait for roster update
    Thread.sleep(1000);

    List<String> contacts = ALICE.superBot().views().sarosView().getContacts();

    assertTrue("corrupted size on roster", contacts.size() >= 2);
    assertEquals(BOB.getBaseJid(), contacts.get(0));
    assertEquals(CARL.getBaseJid(), contacts.get(1));

    checkContactsOrder(contacts, 2);

    BOB.superBot().views().sarosView().disconnect();
    BOB.superBot().views().sarosView().waitUntilIsDisconnected();

    // wait for roster update
    Thread.sleep(1000);

    contacts = ALICE.superBot().views().sarosView().getContacts();

    assertTrue("corrupted size on roster", contacts.size() >= 2);
    assertEquals(CARL.getBaseJid(), contacts.get(0));

    checkContactsOrder(contacts, 1);

    BOB.superBot().views().sarosView().connectWith(BOB.getJID(), BOB.getPassword(), false);
    BOB.superBot().views().sarosView().waitUntilIsConnected();

    // wait for roster update
    Thread.sleep(1000);

    contacts = ALICE.superBot().views().sarosView().getContacts();

    assertTrue("corrupted size on roster", contacts.size() >= 2);
    assertEquals(BOB.getBaseJid(), contacts.get(0));
    assertEquals(CARL.getBaseJid(), contacts.get(1));

    checkContactsOrder(contacts, 2);

    BOB.superBot().views().sarosView().disconnect();
    CARL.superBot().views().sarosView().disconnect();

    // wait for roster update
    Thread.sleep(1000);

    contacts = ALICE.superBot().views().sarosView().getContacts();

    assertTrue("corrupted size on roster", contacts.size() >= 2);
    checkContactsOrder(contacts, 0);
  }

  private void checkContactsOrder(List<String> contacts, int s) {
    for (int i = s; i < contacts.size() - 1; i++)
      assertTrue(
          "roster is not sorted asc. : " + contacts.get(i) + " > " + contacts.get(i + 1),
          contacts.get(i).compareTo(contacts.get(i + 1)) <= 0);
  }
}
