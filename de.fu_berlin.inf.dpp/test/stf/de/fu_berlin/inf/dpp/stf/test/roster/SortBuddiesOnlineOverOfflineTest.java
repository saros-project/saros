package de.fu_berlin.inf.dpp.stf.test.roster;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;

@TestLink(id = "Saros-117_sort_online_buddies_over_offline")
public class SortBuddiesOnlineOverOfflineTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, CARL, BOB);
    }

    @Test
    public void testSortBuddiesOnlineOverOffline() throws Exception {

        // wait for roster update
        Thread.sleep(1000);

        List<String> buddies = ALICE.superBot().views().sarosView()
            .getContacts();

        assertTrue("corrupted size on roster", buddies.size() >= 2);
        assertEquals(BOB.getBaseJid(), buddies.get(0));
        assertEquals(CARL.getBaseJid(), buddies.get(1));

        checkBuddiesOrder(buddies, 2);

        BOB.superBot().views().sarosView().disconnect();
        BOB.superBot().views().sarosView().waitUntilIsDisconnected();

        // wait for roster update
        Thread.sleep(1000);

        buddies = ALICE.superBot().views().sarosView().getContacts();

        assertTrue("corrupted size on roster", buddies.size() >= 2);
        assertEquals(CARL.getBaseJid(), buddies.get(0));

        checkBuddiesOrder(buddies, 1);

        BOB.superBot().views().sarosView()
            .connectWith(BOB.getJID(), BOB.getPassword(), false);
        BOB.superBot().views().sarosView().waitUntilIsConnected();

        // wait for roster update
        Thread.sleep(1000);

        buddies = ALICE.superBot().views().sarosView().getContacts();

        assertTrue("corrupted size on roster", buddies.size() >= 2);
        assertEquals(BOB.getBaseJid(), buddies.get(0));
        assertEquals(CARL.getBaseJid(), buddies.get(1));

        checkBuddiesOrder(buddies, 2);

        BOB.superBot().views().sarosView().disconnect();
        CARL.superBot().views().sarosView().disconnect();

        // wait for roster update
        Thread.sleep(1000);

        buddies = ALICE.superBot().views().sarosView().getContacts();

        assertTrue("corrupted size on roster", buddies.size() >= 2);
        checkBuddiesOrder(buddies, 0);

    }

    private void checkBuddiesOrder(List<String> buddies, int s) {
        for (int i = s; i < buddies.size() - 1; i++)
            assertTrue("roster is not sorted asc. : " + buddies.get(i) + " > "
                + buddies.get(i + 1),
                buddies.get(i).compareTo(buddies.get(i + 1)) <= 0);
    }
}
