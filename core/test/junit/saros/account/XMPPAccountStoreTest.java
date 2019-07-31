package saros.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class XMPPAccountStoreTest {

  @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

  private XMPPAccountStore store;

  @Before
  public void setUp() {
    this.store = new XMPPAccountStore();
  }

  @Test
  public void testWithNoAccountFile() {
    assertTrue(store.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testActiveAccountWithEmptyAccountStore() {
    store.getActiveAccount();
  }

  /*
   * Test whether the currently expected XML format is accepted by the load
   * method. Otherwise saros could became incompatible with previous versions.
   */
  @Test
  public void testAccountsFileFormat() throws Exception {

    final String key = "testKey";

    final XMPPAccount activeAcc =
        new XMPPAccount(
            "activeAccount", "activePwd", "activedomain", "activeserver", 3, false, false);
    final XMPPAccount anotherAcc1 =
        new XMPPAccount(
            "anotherAccount1", "anotherPwd1", "anotherdomain1", "anotherserver1", 1, true, true);
    final XMPPAccount anotherAcc2 =
        new XMPPAccount(
            "anotherAccount2", "anotherPwd2", "anotherdomain2", "anotherserver2", 2, true, true);

    final ArrayList<XMPPAccount> configuredAccounts = new ArrayList<XMPPAccount>();
    configuredAccounts.add(anotherAcc1);
    configuredAccounts.add(activeAcc);
    configuredAccounts.add(anotherAcc2);

    final File tmpAccountFile = tmpFolder.newFile("saros_account.dat");
    final String xmlContent = createAccountFileContent(activeAcc, configuredAccounts);
    writeAccountFile(tmpAccountFile, key, xmlContent);

    store.setAccountFile(tmpAccountFile, key);

    assertEquals(configuredAccounts.size(), store.getAllAccounts().size());

    assertTrue(store.existsAccount("activeAccount", "activedomain", "activeserver", 3));
    assertTrue(store.existsAccount("anotherAccount1", "anotherdomain1", "anotherserver1", 1));
    assertTrue(store.existsAccount("anotherAccount2", "anotherdomain2", "anotherserver2", 2));

    assertEquals("activeAccount", store.getActiveAccount().getUsername());
  }

  private void writeAccountFile(File accountFile, String key, String content) throws Exception {
    final FileOutputStream dataOut = new FileOutputStream(accountFile);
    final byte[] encryptedXmlContent = XMPPAccountStore.Crypto.encrypt(content.getBytes(), key);

    try {
      dataOut.write(encryptedXmlContent);
      dataOut.flush();
    } finally {
      IOUtils.closeQuietly(dataOut);
    }
  }

  private String createAccountFileContent(
      XMPPAccount activeAccount, ArrayList<XMPPAccount> configuredAccounts) {

    int index = configuredAccounts.indexOf(activeAccount);
    StringBuilder xmlContent =
        new StringBuilder()
            .append("<accounts>\n")
            .append(String.format("  <activeAccountIndex>%d</activeAccountIndex>\n", index));

    xmlContent.append("  <configuredAccounts>\n");
    for (XMPPAccount acc : configuredAccounts) {
      xmlContent
          .append("    <xmppAccount>\n")
          .append(String.format("    <username>%s</username>\n", acc.getUsername()))
          .append(String.format("    <password>%s</password>\n", acc.getPassword()))
          .append(String.format("    <domain>%s</domain>\n", acc.getDomain()))
          .append(String.format("    <server>%s</server>\n", acc.getServer()))
          .append(String.format("    <port>%d</port>\n", acc.getPort()))
          .append(String.format("    <useTLS>%s</useTLS>\n", acc.useTLS()))
          .append(String.format("    <useSASL>%s</useSASL>\n", acc.useSASL()))
          .append("    </xmppAccount>\n");
    }
    xmlContent.append("  </configuredAccounts>\n").append("</accounts>");

    return xmlContent.toString();
  }

  @Test
  public void testAutoActivation() throws Exception {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    assertFalse(store.isEmpty());
    assertEquals("a", store.getActiveAccount().getUsername());

    // only the first account must be auto activated
    store.createAccount("b", "b", "b", "b", 1, true, true);
    assertEquals("a", store.getActiveAccount().getUsername());
  }

  @Test
  public void testLoadAccountsWithError() throws IOException {

    File tmpAccountFile = tmpFolder.newFile("saros_account.dat");

    FileOutputStream out = new FileOutputStream(tmpAccountFile);

    byte[] data = new byte[128];

    new Random().nextBytes(data);

    out.write(data);
    out.close();

    store.setAccountFile(tmpAccountFile, null);

    assertTrue(store.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteNonExistingAccount() {
    store.deleteAccount(null);
  }

  @Test
  public void testDeleteExistingAccount() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.createAccount("b", "a", "a", "a", 1, true, true);
    store.deleteAccount(new XMPPAccount("b", "a", "a", "a", 1, true, true));
    assertEquals(1, store.getAllAccounts().size());
  }

  @Test
  public void deleteDefaultAccount() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.deleteAccount(store.getDefaultAccount());
    assertNull(store.getDefaultAccount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateAccount() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.createAccount("a", "a", "a", "a", 1, true, true);
  }

  @Test
  public void setAccountActive() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.setAccountActive(account);
    assertEquals(account, store.getActiveAccount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNonExistingAccountActive() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.deleteAccount(account);
    store.setAccountActive(account);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testChangeAccountDataToAlreadyExistingAccount() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    XMPPAccount account = store.createAccount("b", "a", "a", "a", 1, true, true);
    store.changeAccountData(account, "a", "a", "a", "a", 1, false, false);
  }

  @Test
  public void testChangeAccountData() {
    store.createAccount("a", "a", "a", "a", 1, true, true);
    store.changeAccountData(store.getActiveAccount(), "b", "b", "b", "b", 5, false, false);

    XMPPAccount account = store.getActiveAccount();

    assertEquals("b", account.getPassword());
    assertEquals("b", account.getServer());
    assertEquals("b", account.getDomain());
    assertEquals("b", account.getUsername());
    assertEquals(5, account.getPort());
    assertFalse(account.useTLS());
    assertFalse(account.useSASL());
  }

  @Test
  public void testLoadAndSaveAccounts() throws IOException {
    File tmpAccountFile = tmpFolder.newFile("saros_account.dat");

    store.setAccountFile(tmpAccountFile, null);

    for (int i = 0; i < 10; i++) store.createAccount("" + i, "a", "a", "a", 1, true, true);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    assertEquals(10, store.getAllAccounts().size());
  }

  @Test
  public void testGetServers() {

    for (int i = 0; i < 10; i++)
      for (int j = 0; j < 10; j++) store.createAccount("" + j, "a", "a", "" + i, 1, true, true);

    assertEquals(10, store.getServers().size());
  }

  @Test
  public void testGetDomains() {

    for (int i = 0; i < 10; i++)
      for (int j = 0; j < 10; j++) store.createAccount("" + j, "a", "" + i, "a", 1, true, true);

    assertEquals(10, store.getDomains().size());
  }

  @Test
  public void testComparator() {
    XMPPAccount account0 = store.createAccount("alice", "alice", "b", "b", 1, true, true);
    XMPPAccount account1 = store.createAccount("bob", "bob", "b", "b", 1, true, true);
    XMPPAccount account2 = store.createAccount("alice", "alice", "b", "b", 2, true, true);

    assertEquals(account0, store.getAllAccounts().get(0));
    assertEquals(account2, store.getAllAccounts().get(1));
    assertEquals(account1, store.getAllAccounts().get(2));
  }

  @Test
  public void testChangeAccountDataAndThenDeleteAccount() {
    store.createAccount("alice", "alice", "b", "b", 1, true, true);
    XMPPAccount account1 = store.createAccount("bob", "bob", "b", "b", 1, true, true);
    store.changeAccountData(account1, "a", "a", "a", "a", 5, false, false);
    store.deleteAccount(account1);
    assertEquals(1, store.getAllAccounts().size());
  }

  @Test
  public void testAccountexists() {
    store.createAccount("alice", "alice", "b", "b", 1, true, true);

    assertTrue(store.existsAccount("alice", "b", "b", 1));
    assertFalse(store.existsAccount("Alice", "b", "b", 1));
    assertFalse(store.existsAccount("alice", "a", "b", 1));
    assertFalse(store.existsAccount("alice", "b", "a", 1));
    assertFalse(store.existsAccount("alice", "b", "b", 5));
  }

  @Test
  public void testChangeAccountDataAndActiveAccountAfterDeserialization() throws IOException {

    File tmpAccountFile = tmpFolder.newFile("saros_account.dat");

    store.setAccountFile(tmpAccountFile, null);

    store.createAccount("alice", "alice", "b", "b", 1, true, true);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    XMPPAccount account = store.getActiveAccount();

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
  public void testChangeAccountAfterDeserialization() throws IOException {

    File tmpAccountFile = tmpFolder.newFile("saros_account.dat");

    store.setAccountFile(tmpAccountFile, null);

    store.createAccount("alice", "alice", "b", "b", 1, true, true);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    XMPPAccount defaultAccount = store.getDefaultAccount();

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

    assertEquals(another, defaultAccount);
  }

  @Test
  public void testSetDefaultToNullAndThenDeserializeAgain() throws IOException {
    File tmpAccountFile = tmpFolder.newFile("saros_account.dat");

    XMPPAccount defaultAccount;

    store.setAccountFile(tmpAccountFile, null);

    assertEquals(0, store.getAllAccounts().size());
    // this is the default one
    defaultAccount = store.createAccount("alice", "alice", "b", "b", 1, true, true);
    store.createAccount("bob", "bob", "b", "b", 1, true, true);

    assertEquals(defaultAccount, store.getDefaultAccount());
    store.setDefaultAccount(null);

    store = new XMPPAccountStore();
    store.setAccountFile(tmpAccountFile, null);

    defaultAccount = store.getDefaultAccount();

    assertNull(defaultAccount);

    assertEquals(2, store.getAllAccounts().size());
  }

  @Test
  public void testFindsExistingAccount() {
    XMPPAccount created =
        store.createAccount("alice", "alice", "domain", "server", 12345, true, true);

    XMPPAccount found = store.findAccount("alice@domain");
    assertEquals(created, found);
  }

  @Test
  public void testUnsuccessfulFindAccount() {
    XMPPAccount found = store.findAccount("alice@domain");
    assertNull(found);
  }

  @Test
  public void testFindAccountIgnoresCase() {
    XMPPAccount created =
        store.createAccount("alice", "alice", "domain", "server", 12345, true, true);
    XMPPAccount found = store.findAccount("Alice@Domain");
    assertEquals(created, found);
  }

  @Test(expected = NullPointerException.class)
  public void testFindAccountWithNull() {
    store.findAccount(null);
  }

  @Test
  public void testFindAccountWithEmptyString() {
    XMPPAccount found = store.findAccount("");
    assertNull(found);
  }
}
