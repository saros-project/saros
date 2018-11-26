package de.fu_berlin.inf.dpp.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import org.junit.Test;

public class XMPPAccountStoreTest {

  @Test
  public void testWithNoAccountFile() {
    XMPPAccountStore store = new XMPPAccountStore();
    assertEquals(true, store.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testActiveAccountWithEmptyAccountStore() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.getActiveAccount();
  }

  @Test
  public void testAutoActivation() throws Exception {
    XMPPAccountStore store = new XMPPAccountStore();

    store.createAccount("a", "a", "a", "a", 1, true, true);
    assertEquals(false, store.isEmpty());
    assertEquals("a", store.getActiveAccount().getUsername());

    // only the first account must be auto activated
    store.createAccount("b", "b", "b", "b", 1, true, true);
    assertEquals("a", store.getActiveAccount().getUsername());
  }

  @Test
  public void testLoadAccountsWithError() throws IOException {

    File tmpAccountFile = File.createTempFile("saros_account", ".dat");

    FileOutputStream out = new FileOutputStream(tmpAccountFile);

    byte[] data = new byte[128];

    new Random().nextBytes(data);

    out.write(data);
    out.close();

    XMPPAccountStore store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    assertEquals(true, store.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteNonExistingAccount() {

    XMPPAccountStore store = new XMPPAccountStore();
    store.deleteAccount(null);
  }

  @Test
  public void testDeleteExistingAccount() {

    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.createAccount("b", "a", "a", "a", 1, true, true);
    store.deleteAccount(new XMPPAccount("b", "a", "a", "a", 1, true, true));
    assertEquals(store.getAllAccounts().size(), 1);
  }

  @Test(expected = IllegalStateException.class)
  public void testDeleteActiveAccount() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.deleteAccount(store.getActiveAccount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateAccount() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.createAccount("a", "a", "a", "a", 1, true, true);
  }

  @Test
  public void setAccountActive() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.setAccountActive(account);
    assertEquals(account, store.getActiveAccount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNonExistingAccountActive() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.deleteAccount(account);
    store.setAccountActive(account);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testChangeAccountDataToAlreadyExistingAccount() {

    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.changeAccountData(account, "a", "a", "a", "a", 1, false, false);
  }

  @Test
  public void testChangeAccountData() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.changeAccountData(store.getActiveAccount(), "b", "b", "b", "b", 5, false, false);

    XMPPAccount account = store.getActiveAccount();

    assertEquals(account.getPassword(), "b");
    assertEquals(account.getServer(), "b");
    assertEquals(account.getDomain(), "b");
    assertEquals(account.getUsername(), "b");
    assertEquals(account.getPort(), 5);
    assertEquals(account.useTLS(), false);
    assertEquals(account.useSASL(), false);
  }

  @Test
  public void testLoadAndSaveAccounts() throws IOException {

    File tmpAccountFile = File.createTempFile("saros_account", ".dat");

    XMPPAccountStore store;

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    for (int i = 0; i < 10; i++) store.createAccount("" + i, "a", "a", "a", 1, true, true);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    assertEquals(10, store.getAllAccounts().size());
  }

  @Test
  public void testGetServers() {

    XMPPAccountStore store = new XMPPAccountStore();

    for (int i = 0; i < 10; i++)
      for (int j = 0; j < 10; j++) store.createAccount("" + j, "a", "a", "" + i, 1, true, true);

    assertEquals(10, store.getServers().size());
  }

  @Test
  public void testGetDomains() {

    XMPPAccountStore store = new XMPPAccountStore();

    for (int i = 0; i < 10; i++)
      for (int j = 0; j < 10; j++) store.createAccount("" + j, "a", "" + i, "a", 1, true, true);

    assertEquals(10, store.getDomains().size());
  }

  @Test
  public void testComparator() {
    XMPPAccountStore store = new XMPPAccountStore();
    XMPPAccount account0 = store.createAccount("alice", "alice", "b", "b", 1, true, true);
    XMPPAccount account1 = store.createAccount("bob", "bob", "b", "b", 1, true, true);
    XMPPAccount account2 = store.createAccount("alice", "alice", "b", "b", 2, true, true);

    assertEquals(account0, store.getAllAccounts().get(0));
    assertEquals(account2, store.getAllAccounts().get(1));
    assertEquals(account1, store.getAllAccounts().get(2));
  }

  @Test
  public void testChangeAccountDataAndThenDeleteAccount() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("alice", "alice", "b", "b", 1, true, true);
    XMPPAccount account1 = store.createAccount("bob", "bob", "b", "b", 1, true, true);
    store.changeAccountData(account1, "a", "a", "a", "a", 5, false, false);
    store.deleteAccount(account1);
    assertEquals(1, store.getAllAccounts().size());
  }

  @Test
  public void testAccountexists() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.createAccount("alice", "alice", "b", "b", 1, true, true);

    assertTrue(store.exists("alice", "b", "b", 1));
    assertFalse(store.exists("Alice", "b", "b", 1));
    assertFalse(store.exists("alice", "a", "b", 1));
    assertFalse(store.exists("alice", "b", "a", 1));
    assertFalse(store.exists("alice", "b", "b", 5));
  }

  @Test
  public void testChangeAccountDataAndActiveAccountAfterDeserialization() throws IOException {

    File tmpAccountFile = File.createTempFile("saros_account", ".dat");

    XMPPAccountStore store;

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    XMPPAccount account = store.createAccount("alice", "alice", "b", "b", 1, true, true);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    account = store.getActiveAccount();

    XMPPAccount another = store.getAllAccounts().get(0);

    store.changeAccountData(
        another,
        another.getUsername(),
        another.getPassword(),
        another.getDomain(),
        "",
        0,
        true,
        true);

    assertEquals(another, account);
  }

  @Test
  public void testFindsExistingAccount() {
    XMPPAccountStore store = new XMPPAccountStore();

    XMPPAccount created =
        store.createAccount("alice", "alice", "domain", "server", 12345, true, true);

    XMPPAccount found = store.findAccount("alice@domain");
    assertEquals(created, found);
  }

  @Test
  public void testUnsuccessfulFindAccount() {
    XMPPAccountStore store = new XMPPAccountStore();

    XMPPAccount found = store.findAccount("alice@domain");
    assertEquals(null, found);
  }

  @Test
  public void testFindAccountIgnoresCase() {
    XMPPAccountStore store = new XMPPAccountStore();
    XMPPAccount created =
        store.createAccount("alice", "alice", "domain", "server", 12345, true, true);
    XMPPAccount found = store.findAccount("Alice@Domain");
    assertEquals(created, found);
  }

  @Test(expected = NullPointerException.class)
  public void testFindAccountWithNull() {
    XMPPAccountStore store = new XMPPAccountStore();
    store.findAccount(null);
  }

  @Test
  public void testFindAccountWithEmptyString() {
    XMPPAccountStore store = new XMPPAccountStore();
    XMPPAccount found = store.findAccount("");
    assertEquals(null, found);
  }
}
