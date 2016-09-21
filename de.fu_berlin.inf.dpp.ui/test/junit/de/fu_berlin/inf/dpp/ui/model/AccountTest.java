package de.fu_berlin.inf.dpp.ui.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccountTest {

    @Test
    public void testGetters() {

        final Account account = new Account("alice", "example.org");

        assertEquals("alice", account.getUsername());
        assertEquals("example.org", account.getDomain());
        assertEquals("alice@example.org", account.getBareJid());
    }
}
