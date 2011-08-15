package de.fu_berlin.inf.dpp.accountManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * Class for the management of multiple XMPP accounts in Saros.
 * 
 * @author Stefan Rossbach
 */
@Component(module = "accountManagement")
public final class XMPPAccountStore {

    private static Logger log = Logger.getLogger(XMPPAccountStore.class
        .getName());

    private Set<XMPPAccount> accounts;
    private XMPPAccount activeAccount;
    private IPreferenceStore preferenceStore;
    private ISecurePreferences securePreferenceStore;
    private Integer maxId;

    public XMPPAccountStore(Saros saros) {
        this.preferenceStore = saros.getPreferenceStore();
        this.securePreferenceStore = saros.getSecurePrefs();

        accounts = new HashSet<XMPPAccount>();
        loadAccounts();
    }

    /**
     * Saves the inactive accounts to: username1, password1, server1, username2,
     * password2, ....
     * 
     * The active account is saved as username, password, server.
     */
    public void saveAccounts() {
        boolean encryptAccount = this.preferenceStore
            .getBoolean(PreferenceConstants.ENCRYPT_ACCOUNT);

        // save the inactive accounts (active account is already saved)
        int i = 1;
        for (XMPPAccount account : accounts) {
            if (!account.isActive()) {

                try {
                    this.securePreferenceStore.put(PreferenceConstants.USERNAME
                        + i, account.getUsername(), encryptAccount);

                    this.securePreferenceStore.put(PreferenceConstants.SERVER
                        + i, account.getServer(), encryptAccount);

                    this.securePreferenceStore.put(PreferenceConstants.PASSWORD
                        + i, account.getPassword(), encryptAccount);

                } catch (StorageException e) {
                    log.error("Error while storing account: " + e.getMessage());
                }

                i++;
            }
        }
        // set end-entry (empty string)
        try {
            this.securePreferenceStore.put(PreferenceConstants.USERNAME + i,
                "", encryptAccount);
            this.securePreferenceStore.put(PreferenceConstants.SERVER + i, "",
                encryptAccount);
            this.securePreferenceStore.put(PreferenceConstants.PASSWORD + i,
                "", encryptAccount);
        } catch (StorageException e) {
            log.error("Error while storing account: " + e.getMessage());
        }
    }

    /**
     * Loads the accounts from {@link IPreferenceStore}.
     */
    public void loadAccounts() {
        accounts.clear();
        maxId = 0;

        // load default account (keys: username, password, server)
        String defaultUsername = "";
        String defaultServer = "";
        String defaultPassword = "";

        try {
            defaultUsername = this.securePreferenceStore.get(
                PreferenceConstants.USERNAME, "");
            defaultServer = this.securePreferenceStore.get(
                PreferenceConstants.SERVER, "");
            defaultPassword = this.securePreferenceStore.get(
                PreferenceConstants.PASSWORD, "");
        } catch (StorageException e) {
            log.error("exception while getting default account: "
                + e.getMessage());
        }

        // no default account exist
        if (defaultUsername.length() != 0) {
            XMPPAccount defaultAccount = createNewAccount(defaultUsername,
                defaultPassword, defaultServer);
            setAccountActive(defaultAccount);
        }

        // load the other accounts (keys: username1, password1, ...)

        int i = 1;

        while (true) {
            String username = "";
            String server = "";
            String password = "";

            try {
                username = this.securePreferenceStore.get(
                    PreferenceConstants.USERNAME + i, "");
                server = this.securePreferenceStore.get(
                    PreferenceConstants.SERVER + i, "");
                password = this.securePreferenceStore.get(
                    PreferenceConstants.PASSWORD + i, "");
            } catch (StorageException e) {
                log.error("exception while getting account: " + e.getMessage());
            }

            i++;

            if (username.length() == 0)
                break;

            if (server.length() == 0) {
                log.warn("skipping account '" + username
                    + "', server field is empty");
                continue;
            }

            createNewAccount(username, password, server);
        }
    }

    /**
     * Returns a list containing all accounts.
     * 
     * @return
     */
    public List<XMPPAccount> getAllAccounts() {
        List<XMPPAccount> accounts = new ArrayList<XMPPAccount>(this.accounts);

        Comparator<XMPPAccount> comparator = new Comparator<XMPPAccount>() {

            public int compare(XMPPAccount a, XMPPAccount b) {
                int c = a.getUsername().compareToIgnoreCase(b.getUsername());

                if (c == 0)
                    return a.getServer().compareTo(b.getServer());
                else
                    return c;
            }
        };

        Collections.sort(accounts, comparator);
        return accounts;
    }

    /**
     * Returns a list of all used domains.
     * <p>
     * <b>Example:</b><br/>
     * If the {@link XMPPAccountStore} contains users
     * <ul>
     * <li>alice@jabber.org</li>
     * <li>bob@xyz.com [googlemail.com]</li>
     * <li>carl@saros-con.imp.fu-berlin.de</li>
     * </ul>
     * the server list contains
     * <ul>
     * <li>jabber.org</li>
     * <li>xyz.com</li>
     * <li>saros-con.imp.fu-berlin.de</li>
     * </ul>
     * 
     * @return
     */
    public List<String> getDomains() {
        List<String> domains = new ArrayList<String>();
        for (XMPPAccount account : this.accounts) {
            String username = account.getUsername();
            String domain;
            if (username.contains("@")) {
                domain = username.split("@")[1];
            } else {
                domain = account.getServer();
            }
            if (!domains.contains(domain))
                domains.add(domain);
        }
        return domains;
    }

    /**
     * Returns a list of all used servers.
     * <p>
     * <b>Example:</b><br/>
     * If the {@link XMPPAccountStore} contains users
     * <ul>
     * <li>alice@jabber.org</li>
     * <li>bob@xyz.com [googlemail.com]</li>
     * <li>carl@saros-con.imp.fu-berlin.de</li>
     * </ul>
     * the server list contains
     * <ul>
     * <li>jabber.org</li>
     * <li>googlemail.com</li>
     * <li>saros-con.imp.fu-berlin.de</li>
     * </ul>
     * 
     * @return
     */
    public List<String> getServers() {
        List<String> servers = new ArrayList<String>();
        for (XMPPAccount account : this.accounts) {
            String server = account.getServer();
            if (!servers.contains(server))
                servers.add(server);
        }
        return servers;
    }

    /**
     * Makes the given account active.
     * 
     * @param account
     *            the account to activate
     * @throws IllegalArgumentException
     *             if the account is not found
     */
    public void setAccountActive(XMPPAccount account) {

        if (!accounts.contains(account))
            throw new IllegalArgumentException("account '" + account
                + "' is not in the current account store");

        if (activeAccount != null)
            activeAccount.setActive(false);

        account.setActive(true);
        activeAccount = account;

        updateAccountDataToPreferenceStore();
    }

    private void updateAccountDataToPreferenceStore() {

        if (activeAccount == null)
            return;

        String username = activeAccount.getUsername();
        String server = activeAccount.getServer();
        String password = activeAccount.getPassword();

        /*
         * Google Talk users have to keep their server portion in the username;
         * see http://code.google.com/apis/talk/talk_developers_home.html
         */
        if (server.equalsIgnoreCase("gmail.com")
            || server.equalsIgnoreCase("googlemail.com")) {
            if (!username.contains("@")) {
                username += "@" + server;
            }
        }

        boolean encryptAccount = this.preferenceStore
            .getBoolean(PreferenceConstants.ENCRYPT_ACCOUNT);

        try {
            this.securePreferenceStore.put(PreferenceConstants.USERNAME,
                username, encryptAccount);
            this.securePreferenceStore.put(PreferenceConstants.SERVER, server,
                encryptAccount);
            this.securePreferenceStore.put(PreferenceConstants.PASSWORD,
                password, encryptAccount);
        } catch (StorageException e) {
            log.error("unable to store account '" + activeAccount + "' : "
                + e.getMessage());
        }
    }

    /**
     * Deletes an account.
     * 
     * @param account
     *            the account to delete
     */
    public void deleteAccount(XMPPAccount account) {

        if (!accounts.contains(account))
            throw new IllegalArgumentException("account '" + account
                + "' is not in the current account store");

        accounts.remove(account);
        saveAccounts();
    }

    /**
     * 
     * @param account
     *            the account to check.
     * @return true if the account exists
     */
    public boolean isAccountInStore(XMPPAccount account) {
        return this.accounts.contains(account);
    }

    /**
     * Creates an account with a new id.
     * 
     * @param username
     *            the user name of the new account as lower case string
     * @param password
     *            the password of the new account.
     * @param server
     *            the server of the new account as lower case string
     * @throws NullPointerException
     *             if username, password or server is null
     * @throws IllegalArgumentException
     *             if username or server string is empty or only contains
     *             whitespace characters
     * 
     */

    public XMPPAccount createNewAccount(String username, String password,
        String server) {

        checkCredentials(username, password, server);

        XMPPAccount newAccount = new XMPPAccount(createNewId(), username,
            password, server);

        newAccount.setActive(false);

        this.accounts.add(newAccount);

        return newAccount;
    }

    private void checkCredentials(String username, String password,
        String server) {
        if (username == null)
            throw new NullPointerException("username is null");

        if (password == null)
            throw new NullPointerException("password is null");

        if (server == null)
            throw new NullPointerException("server is null");

        if (username.trim().length() == 0)
            throw new IllegalArgumentException("user name is empty");

        if (server.trim().length() == 0)
            throw new IllegalArgumentException("server is empty");
    }

    private int createNewId() {
        return maxId++;
    }

    /**
     * Returns the account with the given id.
     * 
     * @param id
     *            the id of the searched account
     * @return the account with the given id
     * @throws IllegalArgumentException
     *             if the account does not exists
     */
    public XMPPAccount getAccount(int id) {

        XMPPAccount found = null;

        for (XMPPAccount account : accounts)
            if (account.getId() == id)
                found = account;

        if (found == null)
            throw new IllegalArgumentException("account with id '" + id
                + "' does not exist");

        return found;
    }

    /**
     * Changes the properties of an account.
     * 
     * @param username
     *            the new user name.
     * @param password
     *            the new password.
     * @param server
     *            the new server.
     * 
     * @throws IllegalArgumentException
     *             if the new user name and server already exists
     */
    public void changeAccountData(int id, String username, String password,
        String server) {

        checkCredentials(username, password, server);

        XMPPAccount account = getAccount(id);

        accounts.remove(account);

        XMPPAccount changedAccount = new XMPPAccount(id, username, password,
            server);

        // user changed more than the password
        if (!changedAccount.equals(account)
            && accounts.contains(changedAccount)) {
            accounts.add(account);
            throw new IllegalArgumentException("an account with user name '"
                + username + " and server '" + server + "' already exists");
        }

        account.setUsername(username);
        account.setPassword(password);
        account.setServer(server);

        accounts.add(account);

        if (activeAccount != null && id == activeAccount.getId())
            updateAccountDataToPreferenceStore();

        saveAccounts();
    }

    /**
     * Returns the current active account.
     * 
     * @return the active account
     * @throws IllegalStateException
     *             if no active account exists
     * 
     */
    public XMPPAccount getActiveAccount() {
        if (this.activeAccount == null)
            throw new IllegalStateException(
                "there is currently no active account");

        return this.activeAccount;
    }

    /**
     * 
     * @return <code>true</code> if an active account exists, <code>false</code>
     *         otherwise
     */
    public boolean hasActiveAccount() {
        return activeAccount != null;
    }

    public boolean contains(String username, String server) {
        for (XMPPAccount a : getAllAccounts()) {
            if (a.getServer().equals(server)
                && a.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(JID jid) {
        return contains(jid.getName(), jid.getDomain());
    }

    /**
     * Forces the current account data to be written to the permanent preference
     * store.
     */
    public void flush() {
        updateAccountDataToPreferenceStore();
        saveAccounts();
    }
}
