package de.fu_berlin.inf.dpp.accountManagement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private static final Logger log = Logger.getLogger(XMPPAccountStore.class);

    private int defaultXmppPort;

    private Set<XMPPAccount> accounts;
    private XMPPAccount activeAccount;
    private IPreferenceStore preferenceStore;
    private ISecurePreferences securePreferenceStore;

    @Deprecated
    public XMPPAccountStore(Saros saros) {
        this(saros.getPreferenceStore(), saros.getSecurePrefs());
    }

    public XMPPAccountStore(IPreferenceStore preferenceStore,
        ISecurePreferences securePreferenceStore) {
        this.preferenceStore = preferenceStore;
        this.securePreferenceStore = securePreferenceStore;
        defaultXmppPort = this.preferenceStore
            .getInt(PreferenceConstants.DEFAULT_XMPP_PORT);
        accounts = new HashSet<XMPPAccount>();
        loadAccounts();
    }

    @SuppressWarnings("unchecked")
    private void loadAccounts() {

        byte[] accountData = null;
        byte[] activeAccountData = null;

        try {
            activeAccountData = this.securePreferenceStore.getByteArray(
                PreferenceConstants.ACTIVE_ACCOUNT, null);
        } catch (StorageException e) {
            log.error("error while loading active account", e);
        }

        try {
            accountData = this.securePreferenceStore.getByteArray(
                PreferenceConstants.ACCOUNT_DATA, null);
        } catch (StorageException e) {
            log.error("error while loading accounts", e);
        }

        if (activeAccountData != null) {
            try {
                activeAccount = (XMPPAccount) new ObjectInputStream(
                    new ByteArrayInputStream(activeAccountData)).readObject();

            } catch (Exception e) {
                log.error("error while loading active account", e);
            }
        }

        if (accountData != null) {
            try {
                accounts = (Set<XMPPAccount>) new ObjectInputStream(
                    new ByteArrayInputStream(accountData)).readObject();

            } catch (Exception e) {
                log.error("error while loading accounts", e);
            }
        }

        if (accountData == null)
            accounts = new HashSet<XMPPAccount>();

        if (accounts.isEmpty() && activeAccount != null)
            accounts.add(activeAccount);

        if (activeAccount == null && !accounts.isEmpty())
            activeAccount = accounts.iterator().next();

        log.info("loaded " + accounts.size() + " accounts");
    }

    private void saveAccounts() {

        boolean error = false;

        boolean encryptAccount = this.preferenceStore
            .getBoolean(PreferenceConstants.ENCRYPT_ACCOUNT);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(activeAccount);
            oos.flush();
        } catch (IOException e) {
            log.error(e);
            error = true;
        }

        try {
            this.securePreferenceStore.putByteArray(
                PreferenceConstants.ACTIVE_ACCOUNT, out.toByteArray(),
                encryptAccount);
        } catch (StorageException e) {
            log.debug("error while saving active account", e);
            error = true;
        }

        out.reset();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(accounts);
            oos.flush();
        } catch (IOException e) {
            log.error(e);
            error = true;
        }

        try {
            this.securePreferenceStore.putByteArray(
                PreferenceConstants.ACCOUNT_DATA, out.toByteArray(),
                encryptAccount);
        } catch (StorageException e) {
            log.debug("error while saving accounts", e);
            error = true;
        }

        try {
            securePreferenceStore.flush();
        } catch (IOException e) {
            log.debug("error writing data", e);
            error = true;
        }

        if (!error)
            log.info("saved " + accounts.size() + " accounts");
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

                if (c != 0)
                    return c;

                c = a.getDomain().compareToIgnoreCase(b.getDomain());

                if (c != 0)
                    return c;

                c = a.getServer().compareToIgnoreCase(b.getServer());

                if (c != 0)
                    return c;

                return Integer.valueOf(a.getPort()).compareTo(
                    Integer.valueOf(b.getPort()));
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
            String domain = account.getDomain();
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
     *             if the account is not found in the store
     */
    public void setAccountActive(XMPPAccount account) {

        if (!accounts.contains(account))
            throw new IllegalArgumentException("account '" + account
                + "' is not in the current account store");

        activeAccount = account;

        saveAccounts();
    }

    /**
     * Deletes an account.
     * 
     * @param account
     *            the account to delete
     * @throws IllegalArgumentException
     *             if the account is not found in the store
     * @throws IllegalStateException
     *             if the account is active
     */
    public void deleteAccount(XMPPAccount account) {

        if (!accounts.contains(account))
            throw new IllegalArgumentException("account '" + account
                + "' is not in the current account store");

        if (this.activeAccount == account)
            throw new IllegalStateException("account '" + account
                + "' is active and cannot be deleted");

        accounts.remove(account);

        saveAccounts();
    }

    // TODO remove this method
    @Deprecated
    public XMPPAccount createAccount(String username, String password,
        String server) {
        return createAccount(username, password, server, server,
            defaultXmppPort, true, true);
    }

    /**
     * Creates an account. The account will automatically become active if the
     * account store is empty.
     * 
     * @param username
     *            the user name of the new account as lower case string
     * @param password
     *            the password of the new account.
     * @param domain
     *            the domain name of the server
     * @param server
     *            the server of the new account as lower case string
     * @param port
     *            the port of the server
     * @param useTSL
     *            if the connection should be secured using TSL
     * @param useSASL
     *            if the authentication should be negotiated using SASL
     * @throws NullPointerException
     *             if username, password, domain or server is null
     * @throws IllegalArgumentException
     *             if username, domain or server string is empty or only
     *             contains whitespace characters<br>
     *             if the domain or server contains upper case characters<br>
     *             if the port value is not in range of 1 <= x <= 65535<br>
     *             if an account already exists with the given username,
     *             password, domain, server and port
     */

    public XMPPAccount createAccount(String username, String password,
        String domain, String server, int port, boolean useTSL, boolean useSASL) {

        XMPPAccount newAccount = new XMPPAccount(username, password, domain,
            server, port, useTSL, useSASL);

        if (this.accounts.contains(newAccount))
            throw new IllegalArgumentException("account already exists");

        if (this.accounts.isEmpty())
            this.activeAccount = newAccount;

        this.accounts.add(newAccount);

        saveAccounts();

        return newAccount;
    }

    @Deprecated
    public void changeAccountData(XMPPAccount account, String username,
        String password, String server) {
        changeAccountData(account, username, password, account.getDomain(),
            server, account.getPort(), account.useTSL(), account.useSASL());
    }

    /**
     * Changes the properties of an account.
     * 
     * @param username
     *            the new user name
     * @param password
     *            the new password
     * @param domain
     *            the domain name of the server
     * @param server
     *            the server ip / name
     * @param port
     *            the port of the server
     * @param useTSL
     *            if the connection should be secured using TSL
     * @param useSASL
     *            if the authentication should be negotiated using SASL
     * @throws IllegalArgumentException
     *             if username, domain or server string is empty or only
     *             contains whitespace characters<br>
     *             if the domain or server contains upper case characters<br>
     *             if the port value is not in range of 1 <= x <= 65535<br>
     *             if an account already exists with the given username,
     *             password, domain, server and port
     */
    public void changeAccountData(XMPPAccount account, String username,
        String password, String domain, String server, int port,
        boolean useTSL, boolean useSASL) {

        XMPPAccount changedAccount = new XMPPAccount(username, password,
            domain, server, port, useTSL, useSASL);

        accounts.remove(account);

        if (accounts.contains(changedAccount)) {
            accounts.add(account);
            throw new IllegalArgumentException("an account with user name '"
                + username + "', domain '" + domain + "' and server '" + server
                + "' with port '" + port + "' already exists");
        }

        account.setUsername(username);
        account.setPassword(password);
        account.setDomain(domain);
        account.setServer(server);
        account.setPort(port);
        account.setUseSASL(useSASL);
        account.setUseTSL(useTSL);

        accounts.add(account);

        saveAccounts();
    }

    /**
     * Returns the current active account.
     * 
     * @return the active account
     * @throws IllegalStateException
     *             if the account store is empty
     * 
     */
    public XMPPAccount getActiveAccount() {
        if (this.activeAccount == null)
            throw new IllegalStateException("the account store is empty");

        return this.activeAccount;
    }

    /**
     * Returns if the account store is currently empty
     * 
     * @return <code>true</code> if the account store is empty,
     *         <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return this.accounts.isEmpty();
    }

    @Deprecated
    public boolean exists(JID jid) {
        return exists(jid.getName(), jid.getDomain());
    }

    @Deprecated
    public boolean exists(String username, String server) {
        for (XMPPAccount a : getAllAccounts()) {
            if (a.getServer().equalsIgnoreCase(server)
                && a.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the an account with the given arguments exists in the account
     * store
     * 
     * @param username
     *            the username
     * @param password
     *            the password
     * @param domain
     *            the domain name of the server
     * @param server
     *            the server ip / name
     * @param port
     *            the port of the server
     * @return <code>true if such an account exists, <code>false</code>
     *         otherwise
     */
    public boolean exists(String username, String password, String domain,
        String server, int port) {
        for (XMPPAccount a : getAllAccounts()) {
            if (a.getServer().equalsIgnoreCase(server)
                && a.getPassword().equals(password)
                && a.getDomain().equalsIgnoreCase(domain)
                && a.getUsername().equals(username) && a.getPort() == port) {
                return true;
            }
        }
        return false;
    }
}
