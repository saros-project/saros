package de.fu_berlin.inf.dpp.accountManagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class XMPPAccountTest {

    XMPPAccount alice0 = new XMPPAccount(0, "alice", "alice", "localhost");
    XMPPAccount alice1 = new XMPPAccount(0, "alice", "ALICE", "localhost");
    XMPPAccount alice2 = new XMPPAccount(0, "ALICE", "ALICE", "localhost");

    XMPPAccount bob = new XMPPAccount(0, "bob", "bob", "localhost");

    @Test
    public void testXMPPAccountGetSet() {

        assertEquals(alice0.getId(), 0);
        assertEquals(alice0.getPassword(), "alice");
        assertEquals(alice0.getServer(), "localhost");
        assertEquals(alice0.getUsername(), "alice");

        alice1.setActive(true);
        assertTrue(alice1.isActive());

        // make code coverage happy
        alice1.toString();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAccountDataServer() {
        new XMPPAccount(0, "Alice", "alice", "localhosT");
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidAccountNullName() {
        new XMPPAccount(0, null, "alice", "localhost");
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidAccountNullPassword() {
        new XMPPAccount(0, "alice", null, "localhost");
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidAccountNullServer() {
        new XMPPAccount(0, "alice", "alice", null);
    }

    @Test
    public void testEqualAndHashCode() {
        Set<XMPPAccount> accounts = new HashSet<XMPPAccount>();

        accounts.add(alice0);
        accounts.add(alice1);
        accounts.add(alice2);
        accounts.add(bob);

        assertEquals(3, accounts.size());

        XMPPAccount b0 = new XMPPAccount(0, "b", "b", "b");
        XMPPAccount b1 = new XMPPAccount(0, "b", "b", "a");
        assertFalse(b0.equals(null));
        assertFalse(b0.equals(new StringBuilder()));
        assertFalse(b0.equals(b1));
    }

    @Test
    public void testToString() {
        XMPPAccount a = new XMPPAccount(0, "a@a", "a", "a");
        XMPPAccount b = new XMPPAccount(0, "b", "b", "b");

        assertEquals("a@a[a]", a.toString());
        assertEquals("b@b", b.toString());
    }

}
