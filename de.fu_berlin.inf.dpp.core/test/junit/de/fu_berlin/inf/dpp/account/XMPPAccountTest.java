package de.fu_berlin.inf.dpp.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class XMPPAccountTest {

  XMPPAccount alice0 = new XMPPAccount("alice", "alice", "localhost", "localhost", 1, true, true);
  XMPPAccount alice1 = new XMPPAccount("alice", "ALICE", "localhost", "localhost", 1, true, true);
  XMPPAccount alice2 = new XMPPAccount("ALICE", "ALICE", "localhost", "localhost", 1, true, true);

  XMPPAccount bob = new XMPPAccount("bob", "bob", "localhost", "localhost", 1, true, true);

  @Test
  public void testXMPPAccountGetSet() {

    assertEquals(alice0.getPassword(), "alice");
    assertEquals(alice0.getServer(), "localhost");
    assertEquals(alice0.getDomain(), "localhost");
    assertEquals(alice0.getUsername(), "alice");
    assertEquals(alice0.getPort(), 1);
    assertEquals(alice0.useTLS(), true);
    assertEquals(alice0.useSASL(), true);

    // make code coverage happy
    alice1.toString();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUsername() {
    new XMPPAccount("", "alice", "localhost", "localhosT", 1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDomain() {
    new XMPPAccount("alice", "alice", "", "localhosT", 1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsingNoServerButPort() {
    new XMPPAccount("alice", "alice", "localhost", "", 1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAccountDataServer() {
    new XMPPAccount("alice", "alice", "localhost", "localhosT", 1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAccountDataDomain() {
    new XMPPAccount("alice", "alice", "localhosT", "localhost", 1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAccountDataPortUnderflow() {
    new XMPPAccount("alice", "alice", "localhost", "localhost", -1, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUsingServerWithoutPort() {
    new XMPPAccount("alice", "alice", "localhost", "localhost", 0, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAccountDataPortOverflow() {
    new XMPPAccount("alice", "alice", "localhost", "localhost", 65536, true, true);
  }

  @Test(expected = NullPointerException.class)
  public void testInvalidAccountNullName() {
    new XMPPAccount(null, "alice", "localhost", "localhost", 1, true, true);
  }

  @Test(expected = NullPointerException.class)
  public void testInvalidAccountNullPassword() {
    new XMPPAccount("alice", null, "localhost", "localhost", 1, true, true);
  }

  @Test(expected = NullPointerException.class)
  public void testInvalidAccountNullDomain() {
    new XMPPAccount("alice", "alice", null, "localhost", 1, true, true);
  }

  @Test(expected = NullPointerException.class)
  public void testInvalidAccountNullServer() {
    new XMPPAccount("alice", "alice", "localhost", null, 1, true, true);
  }

  @Test
  public void testEqualAndHashCode() {
    Set<XMPPAccount> accounts = new HashSet<XMPPAccount>();

    accounts.add(alice0);
    accounts.add(alice1);
    accounts.add(alice2);
    accounts.add(bob);
    accounts.add(alice0);
    accounts.add(alice1);
    accounts.add(alice2);
    accounts.add(bob);

    assertEquals(3, accounts.size());

    assertTrue(alice0.equals(alice0));

    // different port = different service
    assertFalse(
        alice0.equals(new XMPPAccount("alice", "alice", "localhost", "localhost", 2, true, true)));

    assertFalse(alice0.equals(null));
    assertFalse(alice0.equals(new StringBuilder()));
    assertTrue(alice0.equals(alice1));
  }
}
