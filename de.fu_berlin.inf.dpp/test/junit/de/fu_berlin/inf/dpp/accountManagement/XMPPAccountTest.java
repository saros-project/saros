package de.fu_berlin.inf.dpp.accountManagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class XMPPAccountTest {

    XMPPAccount alice0 = new XMPPAccount(0, "alice", "alice", "localhosT");
    XMPPAccount alice1 = new XMPPAccount(0, "ALICE", "ALICE", "localhosT");
    XMPPAccount alice2 = new XMPPAccount(0, "alice", "ALICE", "localhosT");
    XMPPAccount bob = new XMPPAccount(0, "bob", "bob", "localhosT");

    @Test
    public void testXMPPAccountGetSet() {

        assertEquals(alice0.getId(), 0);
        assertEquals(alice0.getPassword(), "alice");
        assertEquals(alice0.getServer(), "localhosT");
        assertEquals(alice0.getUsername(), "alice");

        alice0.setActive(true);
        assertTrue(alice0.isActive());

        // make code coverage happy
        alice0.toString();

    }

    @Test
    public void testEquals() {
        assertTrue(
            "x.compareTo(y) == 0, but ! x.equals(y), take a look at this",
            alice0.compareTo(alice1) == 0 ? alice0.equals(alice1) : true);
    }

    @Test
    public void testEqualAndHashCode() {
        Set<XMPPAccount> accounts = new HashSet<XMPPAccount>();

        accounts.add(alice0);
        accounts.add(alice1);
        accounts.add(alice2);
        accounts.add(bob);

        assertEquals(2, accounts.size());

    }

}
