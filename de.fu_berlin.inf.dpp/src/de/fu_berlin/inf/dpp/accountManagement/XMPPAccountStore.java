package de.fu_berlin.inf.dpp.accountManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * Class for the management of multiple XMPP accounts in Saros.
 */
@Component(module = "accountManagement")
public class XMPPAccountStore {

    private List<XMPPAccount> accounts;
    private XMPPAccount activeAccount;
    private IPreferenceStore preferenceStore;
    private Integer maxId;

    public XMPPAccountStore(Saros saros) {
        this.preferenceStore = saros.getPreferenceStore();
        accounts = new ArrayList<XMPPAccount>();
        loadAccounts();
    }

    /**
     * Saves the inactive accounts to: username1, password1, server1, username2,
     * password2, ....
     * 
     * The active account is saved as username, password, server.
     */
    public void saveAccounts() {
        // save the inactive accounts (active account is already saved)
        int i = 1;
        for (XMPPAccount account : accounts) {
            if (!account.isActive) {
                this.preferenceStore.setValue(PreferenceConstants.USERNAME + i,
                    account.getUsername());
                this.preferenceStore.setValue(PreferenceConstants.PASSWORD + i,
                    account.getPassword());
                this.preferenceStore.setValue(PreferenceConstants.SERVER + i,
                    account.getServer());
                i++;
            }
        }
        // set end-entry (empty string)
        this.preferenceStore.setValue(PreferenceConstants.USERNAME + i, "");
        this.preferenceStore.setValue(PreferenceConstants.PASSWORD + i, "");
        this.preferenceStore.setValue(PreferenceConstants.SERVER + i, "");
    }

    /**
     * Loads the accounts from {@link IPreferenceStore}.
     */
    public void loadAccounts() {
        accounts.clear();
        maxId = 0;

        // load default account (keys: username, password, server)
        String defaultUsername = preferenceStore
            .getString(PreferenceConstants.USERNAME);
        String defaultPassword = preferenceStore
            .getString(PreferenceConstants.PASSWORD);
        String defaultServer = preferenceStore
            .getString(PreferenceConstants.SERVER);

        // no default account exist
        if (defaultUsername.length() < 1) {
            return;
        } else {
            XMPPAccount defaultAccount = createNewAccount(defaultUsername,
                defaultPassword, defaultServer);
            setAccountActive(defaultAccount);
        }

        // load the other accounts (keys: username1, password1, ...)
        int i = 1;
        boolean noMoreUserFound = false;
        do {
            String username = preferenceStore
                .getString(PreferenceConstants.USERNAME + i);
            String password = preferenceStore
                .getString(PreferenceConstants.PASSWORD + i);
            String server = preferenceStore
                .getString(PreferenceConstants.SERVER + i);
            i++;
            if (username.length() < 1) {
                noMoreUserFound = true;
            } else {
                createNewAccount(username, password, server);
            }
        } while (!noMoreUserFound);
    }

    /**
     * Return true if an account exists in {@link IPreferenceStore}.
     * 
     * @return true if an account exist.
     */
    public boolean accountsInPreferenceExist() {
        String username = preferenceStore
            .getString(PreferenceConstants.USERNAME);
        if (username.length() > 2) {
            return true;
        }
        return false;
    }

    /**
     * Returns a list containing all accounts.
     * 
     * @return
     */
    public List<XMPPAccount> getAllAccounts() {
        Collections.sort(this.accounts);
        return Collections.unmodifiableList(this.accounts);
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
     * </ul>.
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
     * </ul>.
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
     *            the account to activate.
     */
    public void setAccountActive(XMPPAccount account) {
        int accountToActivateId = account.getId();
        int cntActiveAccounts = 0;
        for (XMPPAccount accountInList : accounts) {
            int accountInListID = accountInList.getId();
            if (accountInListID == accountToActivateId) {
                accountInList.setActive(true);
                activeAccount = accountInList;
                cntActiveAccounts++;
            } else {
                accountInList.setActive(false);
            }
        }
        checkActiveAccountCount(cntActiveAccounts);
        updateAccountDataToPreferenceStore();
    }

    protected void checkActiveAccountCount(int cntActiveAccounts) {
        if (cntActiveAccounts < 1) {
            throw new IllegalStateException("Less then one active Account");
        } else if (cntActiveAccounts > 1) {
            throw new IllegalStateException("More the one active Account");
        }
    }

    protected void updateAccountDataToPreferenceStore() {
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

        preferenceStore.setValue(PreferenceConstants.USERNAME, username);
        preferenceStore.setValue(PreferenceConstants.SERVER, server);
        preferenceStore.setValue(PreferenceConstants.PASSWORD, password);
    }

    /**
     * Deletes the account from list.
     * 
     * @param account
     *            the account to delete.
     */
    public void deleteAccount(XMPPAccount account) {
        if (isAccountInList(account)) {
            this.accounts.remove(account);
            saveAccounts();
        } else {
            throw new IllegalArgumentException("Account is not in List");
        }
    }

    /**
     * 
     * @param account
     *            the account to check.
     * @return true if the account exists in list.
     */
    public boolean isAccountInList(XMPPAccount account) {
        return this.accounts.contains(account);
    }

    protected boolean isAccountInList(int accountId) {
        XMPPAccount found = getAccount(accountId);
        return isAccountInList(found);
    }

    /**
     * Creates an account with a new id.
     * 
     * @param username
     *            the username of the new account.
     * @param password
     *            the password of the new account.
     */
    public XMPPAccount createNewAccount(String username, String password,
        String server) {
        XMPPAccount newAccount = new XMPPAccount(createNewId(), username,
            password, server);
        newAccount.setActive(false);
        this.accounts.add(newAccount);
        return newAccount;
    }

    /**
     * Returns a new id.
     * 
     * @return a new id.
     */
    protected int createNewId() {
        return maxId++;
    }

    /**
     * Returns the account with the given id.
     * 
     * @param id
     *            the id of the searched account.
     * @return the account with the given id.
     */
    public XMPPAccount getAccount(int id) {
        XMPPAccount found = null;
        for (XMPPAccount account : accounts) {
            if (account.getId() == id) {
                found = account;
            }
        }
        if (found == null) {
            handleAccountNotInList(id);
        }
        return found;
    }

    /**
     * Changes the properties of an account.
     * 
     * @param username
     *            the new username.
     * @param password
     *            the new password.
     * @param server
     *            the new server.
     */
    public void changeAccountData(int id, String username, String password,
        String server) {
        if (!isAccountInList(id)) {
            handleAccountNotInList(id);
        }
        XMPPAccount accountToChange = getAccount(id);
        accountToChange.setUsername(username);
        accountToChange.setPassword(password);
        accountToChange.setServer(server);
        if (id == activeAccount.getId()) {
            updateAccountDataToPreferenceStore();
        }
        saveAccounts();
    }

    protected void handleAccountNotInList(int id) {
        throw new IllegalArgumentException("Account with id " + id
            + " does not exist.");
    }

    public XMPPAccount getActiveAccount() {
        return this.activeAccount;
    }

    public boolean hasActiveAccount() {
        return activeAccount != null;
    }
}
