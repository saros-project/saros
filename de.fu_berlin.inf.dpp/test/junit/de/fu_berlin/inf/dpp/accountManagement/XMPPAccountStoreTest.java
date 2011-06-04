package de.fu_berlin.inf.dpp.accountManagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.test.util.MemorySecurePreferences;

public class XMPPAccountStoreTest {

    private static class SarosStub extends Saros {

        PreferenceStore preferenceStore;
        MemorySecurePreferences securePreferences;

        public SarosStub() throws IOException {
            preferenceStore = new PreferenceStore(File.createTempFile(
                "preference_store", null).getAbsolutePath());

            /*
             * String filename = System.getProperty("java.io.tmpdir");
             * 
             * URL url;
             * 
             * if (filename.startsWith("/")) url = new URL("file://" +
             * filename); else url = new URL("file:///" + filename);
             * System.out.println(url.toString()); // securePreferences =
             * SecurePreferencesFactory.open(url, null);
             */

            securePreferences = new MemorySecurePreferences();

        }

        @Override
        public IPreferenceStore getPreferenceStore() {
            return this.preferenceStore;
        }

        @Override
        public ISecurePreferences getSecurePrefs() {
            return this.securePreferences;
        }
    }

    private static SarosStub saros;

    private void createDefaultUser(ISecurePreferences pref, String name,
        String password, String server) {
        try {
            pref.put(PreferenceConstants.USERNAME, name, true);
            pref.put(PreferenceConstants.PASSWORD, password, true);
            pref.put(PreferenceConstants.SERVER, server, true);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void createSaros() throws IOException {
        saros = new SarosStub();
    }

    @AfterClass
    public static void deleteSaros() {
        saros = null; // help gc
    }

    @Test
    public void testWithEmptyPreferenceStore() {
        XMPPAccountStore store = new XMPPAccountStore(saros);
        assertEquals(store.getAllAccounts().size(), 0);
    }

    @Test
    public void testWithDefaultUser() {
        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        assertEquals(store.getAllAccounts().size(), 1);
        List<XMPPAccount> list = store.getAllAccounts();
        XMPPAccount account = list.get(0);
        store.saveAccounts();

        assertEquals("alice", account.getUsername());
        assertEquals("alice", account.getPassword());
        assertEquals("localhost", account.getServer());

        assertTrue(store.accountsInPreferenceExist());

    }

    @Test
    public void testLoadAccountsWithError() {
        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        saros.securePreferences.allowGetOperation(false);

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.saveAccounts();

        assertEquals(store.getAllAccounts().size(), 0);

        assertFalse(store.accountsInPreferenceExist());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNonExistingAccount() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.deleteAccount(null);

    }

    @Test
    public void testDeleteExistingAccount() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        XMPPAccount account = store
            .createNewAccount("bob", "der", "baumeister");
        store.deleteAccount(account);
        assertEquals(store.getAllAccounts().size(), 1);

    }

    @Test
    public void testCreateDuplicateAccount() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.createNewAccount("alice", "alice_foo", "localhost");
        store.saveAccounts();

        assertEquals(1, store.getAllAccounts().size());
    }

    @Test
    public void testJIDContains() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.createNewAccount("bob", "bob", "localhost");
        store.saveAccounts();

        assertTrue(store.contains(new JID("alice@localhost")));
        assertTrue(store.contains(new JID("bob@localhost")));
        assertFalse(store.contains(new JID("nothing@all")));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeAccountDataOnNonExistingAccount() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.createNewAccount("bob", "bob", "localhost");
        store.changeAccountData(Integer.MIN_VALUE, "non", "valid", "user");
    }

    @Test
    public void testChangeAccountData() {

        saros.securePreferences = new MemorySecurePreferences();

        createDefaultUser(saros.securePreferences, "alice", "alice",
            "localhost");

        XMPPAccountStore store = new XMPPAccountStore(saros);
        XMPPAccount account = store.createNewAccount("bob", "bob", "localhost");

        store.setAccountActive(account);
        store.changeAccountData(account.getId(), "carl", "carl", "local");

        assertEquals("carl", account.getUsername());
        assertEquals("carl", account.getPassword());
        assertEquals("local", account.getServer());

    }

    @Test(expected = IllegalStateException.class)
    public void testActivateNonExistingAccountOnEmptyStore() {

        saros.securePreferences = new MemorySecurePreferences();

        XMPPAccountStore store = new XMPPAccountStore(saros);
        XMPPAccount account = new XMPPAccount(0, "bob", "bob", "localhost");

        store.setAccountActive(account);
    }

    @Test
    public void testActivateMultipleAccount() {

        saros.securePreferences = new MemorySecurePreferences();

        XMPPAccountStore store = new XMPPAccountStore(saros);
        XMPPAccount a = store.createNewAccount("alice", "alice", "localhost");
        XMPPAccount b = store.createNewAccount("bob", "bob", "localhost");
        XMPPAccount c = store.createNewAccount("carl", "carl", "localhost");

        saros.securePreferences.allowPutOperation(false);
        store.saveAccounts(); // for code coverage

        store.setAccountActive(a);
        store.setAccountActive(b);
        store.setAccountActive(c);
        assertTrue(store.hasActiveAccount());
        assertEquals(store.getActiveAccount(), c);
    }

    @Test
    public void testGoogleExtension() throws StorageException {
        saros.securePreferences = new MemorySecurePreferences();

        XMPPAccountStore store = new XMPPAccountStore(saros);
        XMPPAccount a = store.createNewAccount("alice", "alice",
            "googlemail.com");

        store.setAccountActive(a);
        store.setAccountActive(a);

        assertEquals("alice@googlemail.com",
            saros.securePreferences.get(PreferenceConstants.USERNAME, "n/a"));
    }

    @Test
    public void testGetServers() {
        saros.securePreferences = new MemorySecurePreferences();

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.createNewAccount("alice", "alice", "googlemail.com");
        store.createNewAccount("josh@bloch.com", "java", "gmail.com");
        store.createNewAccount("papa", "schlumpf", "schlumpfhausen.dorf");

        List<String> servers = store.getServers();

        assertTrue("alice = googlemail.com", servers.contains("googlemail.com"));
        assertTrue("josh =  gmail.com", servers.contains("gmail.com"));
        assertTrue("papa = schlumpfhause.dorf",
            servers.contains("schlumpfhausen.dorf"));
    }

    @Test
    public void testGetDomains() {
        saros.securePreferences = new MemorySecurePreferences();

        XMPPAccountStore store = new XMPPAccountStore(saros);
        store.createNewAccount("alice", "alice", "googlemail.com");
        store.createNewAccount("bob@xyz.com", "bob", "googlemail.com");
        store.createNewAccount("josh@bloch.com", "java", "gmail.com");
        store.createNewAccount("papa", "schlumpf", "schlumpfhausen.dorf");
        store.createNewAccount("bob@xyz.com", "bob", "googlemail.com");

        List<String> servers = store.getDomains();

        assertTrue("alice = googlemail.com", servers.contains("googlemail.com"));
        assertTrue("bob =  xyz.com", servers.contains("xyz.com"));
        assertTrue("josh =  bloch.com", servers.contains("bloch.com"));
        assertTrue("papa = schlumpfhause.dorf",
            servers.contains("schlumpfhausen.dorf"));
    }

}
