package de.fu_berlin.inf.dpp.accountManagement;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * Class for the management of multiple XMPP accounts in Saros.
 */
@Component(module = "accountManagement")
public class XMPPAccountStore {

    private ArrayList<XMPPAccount> accounts;
    private XMPPAccount activeAccount;
    private IPreferenceStore preferenceStore;
    private Integer maxId = 0;

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
                this.preferenceStore.setValue("username" + i,
                    account.getUsername());
                this.preferenceStore.setValue("password" + i,
                    account.getPassword());
                this.preferenceStore
                    .setValue("server" + i, account.getServer());
                i++;
            }
        }
        // set end-entry (empty string)
        this.preferenceStore.setValue("username" + i, "");
        this.preferenceStore.setValue("password" + i, "");
        this.preferenceStore.setValue("server" + i, "");
    }

    /**
     * Loads the accounts from {@link IPreferenceStore}.
     */
    public void loadAccounts() {
        accounts.clear();
        // load default account (keys: username, password, server)
        String defaultUsername = preferenceStore.getString("username");
        String defaultPassword = preferenceStore.getString("password");
        String defaultServer = preferenceStore.getString("server");

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
            String username = preferenceStore.getString("username" + i);
            String password = preferenceStore.getString("password" + i);
            String server = preferenceStore.getString("server" + i);
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
     * Checks if the managers state is valid.
     */
    protected void checkInvariants() {
        boolean isValid = (activeAccount != null) && (accounts.size() > 0)
            && (preferenceStore != null);

        if (!isValid)
            throw new IllegalStateException(
                "XMPPAccountManager has invalid state!");
    }

    public ArrayList<XMPPAccount> getAllAccounts() {
        return this.accounts;
    }

    public boolean isJabberAccountActive(XMPPAccount account) {
        return account.isActive();
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
        setAccountDataToPreferenceStore(activeAccount.getUsername(),
            activeAccount.getServer(), activeAccount.getPassword());
    }

    protected void checkActiveAccountCount(int cntActiveAccounts) {
        if (cntActiveAccounts < 1) {
            throw new IllegalStateException("Less then one active Account");
        } else if (cntActiveAccounts > 1) {
            throw new IllegalStateException("More the one active Account");
        }
    }

    /**
     * 
     * @param username
     *            the username to store.
     * @param server
     *            the servername to store.
     * @param password
     *            the password to store.
     */
    protected void setAccountDataToPreferenceStore(String username,
        String server, String password) {
        preferenceStore.setValue(PreferenceConstants.USERNAME,
            activeAccount.getUsername());
        preferenceStore.setValue(PreferenceConstants.SERVER,
            activeAccount.getServer());
        preferenceStore.setValue(PreferenceConstants.PASSWORD,
            activeAccount.getPassword());
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
        XMPPAccount newAccount = new XMPPAccount(username, password, server);
        newAccount.setActive(false);
        newAccount.setId(createNewId());
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
            setAccountDataToPreferenceStore(accountToChange.getUsername(),
                accountToChange.getServer(), accountToChange.getPassword());
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
