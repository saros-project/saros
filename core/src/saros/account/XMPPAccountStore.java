package saros.account;

import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.annotations.Component;
import saros.misc.xstream.XStreamFactory;
import saros.net.xmpp.JID;

/**
 * The XMPPAccountStore is responsible for administering XMPP account credentials. All data will
 * just reside in memory unless {@link #setAccountFile(File, String)} is called. This call should
 * only be made once and <b>before</b> any account manipulation is done.
 *
 * <p>Provides events for {@link IAccountStoreListener}s.
 *
 * <p><b>Note:</b> Although this class is thread safe it is <b>not</b> recommended to manipulate and
 * use data from different threads.
 */
@Component(module = "account")
public final class XMPPAccountStore {
  private static final Logger log = Logger.getLogger(XMPPAccountStore.class);

  private static final long MAX_ACCOUNT_DATA_SIZE = 10 * 1024 * 1024;

  private static final String DEFAULT_SECRET_KEY = "Saros";

  private final CopyOnWriteArrayList<IAccountStoreListener> listeners =
      new CopyOnWriteArrayList<IAccountStoreListener>();

  private Set<XMPPAccount> accounts;
  private XMPPAccount defaultAccount;

  private File accountFile;
  private String secretKey;

  public XMPPAccountStore() {
    setAccountFile(null, null);
  }

  /**
   * Registers a listener which will be notified on changes to the list of stored accounts, in
   * particular the addition, deletion, and altering of accounts, as well as changing the currently
   * active account.
   *
   * @param listener will only be added once
   */
  public void addListener(IAccountStoreListener listener) {
    listeners.addIfAbsent(listener);
  }

  /**
   * Unregister a listener to be no longer notified about account store changes.
   *
   * @param listener will no longer be notified, if it was registered before
   */
  public void removeListener(IAccountStoreListener listener) {
    listeners.remove(listener);
  }

  private void notifyAccountStoreListeners() {
    List<XMPPAccount> allAccounts = getAllAccounts();
    for (IAccountStoreListener listener : listeners) {
      listener.accountsChanged(allAccounts);
    }
  }

  private void notifyActiveAccountListeners() {
    for (IAccountStoreListener listener : listeners) {
      listener.activeAccountChanged(defaultAccount);
    }
  }

  /**
   * Sets the account file where the store should search for accounts. This method should be used
   * with care as it discards all currently loaded accounts which may result in unexpected behavior
   * if account objects are already in use. The store will be refreshed with possible new data when
   * this method is called. All missing directories will be created on demand. <b>Note:</b> Although
   * the file is encrypted it is highly recommended to use a file location that is only accessible
   * by the current user.
   *
   * @param file the file to load and store account data or <code>null</code> to just enable in
   *     memory account management
   * @param key the key for encryption and decryption of the file or <code>null</code> to use the
   *     default key
   */
  public void setAccountFile(final File file, final String key) {
    synchronized (this) {
      accountFile = file;
      secretKey = key;

      if (secretKey == null) secretKey = DEFAULT_SECRET_KEY;

      accounts = new HashSet<XMPPAccount>();
      defaultAccount = null;

      if (accountFile != null) {
        File parent = accountFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
          log.error("could not create directories for file: " + file.getAbsolutePath());
          accountFile = null;
        }
      }

      loadAccounts();
    }

    notifyAccountStoreListeners();
    notifyActiveAccountListeners();
  }

  private synchronized void loadAccounts() {

    if (accountFile == null || !accountFile.exists()) return;

    log.debug("loading accounts from file: " + accountFile.getAbsolutePath());

    FileInputStream dataIn = null;

    try {
      long accountFileSize = accountFile.length();
      if (accountFileSize <= 0 || accountFileSize > MAX_ACCOUNT_DATA_SIZE)
        throw new IOException(
            "account data seems malformed, refused to load "
                + accountFileSize
                + " bytes into memory");

      dataIn = new FileInputStream(accountFile);

      byte[] encryptedAccountData = IOUtils.toByteArray(dataIn);

      XStream xStream = createXStream();
      AccountStoreInformation accountData =
          (AccountStoreInformation)
              xStream.fromXML(
                  new ByteArrayInputStream(Crypto.decrypt(encryptedAccountData, secretKey)));

      accounts = new HashSet<>(accountData.configuredAccounts);
      defaultAccount = null;

      if (accountData.activeAccountIndex != -1)
        defaultAccount = accountData.configuredAccounts.get(accountData.activeAccountIndex);

    } catch (RuntimeException e) {
      log.error("internal error while loading account data", e);
      return;
    } catch (Exception e) {
      log.error("could not load account data", e);
      return;
    } finally {
      IOUtils.closeQuietly(dataIn);
    }

    log.debug("loaded " + accounts.size() + " account(s)");
  }

  private synchronized void saveAccounts() {

    if (accountFile == null) return;

    if (!accountFile.exists()) {
      try {
        accountFile.createNewFile();
      } catch (IOException e) {
        log.error(
            "could not create file: " + accountFile.getAbsolutePath() + " to store account data");
        return;
      }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    XStream xStream = createXStream();

    FileOutputStream dataOut = null;

    try {
      dataOut = new FileOutputStream(accountFile);
      // use a pair in order to create a artificial xml root node
      ArrayList<XMPPAccount> accountsToSave = new ArrayList<>(accounts);

      int defaultAccountIndex =
          defaultAccount == null ? -1 : accountsToSave.indexOf(defaultAccount);

      xStream.toXML(new AccountStoreInformation(defaultAccountIndex, accountsToSave), out);

      byte[] encryptedAccountData = Crypto.encrypt(out.toByteArray(), secretKey);

      dataOut.write(encryptedAccountData);
      dataOut.flush();

    } catch (RuntimeException e) {
      log.error("internal error while storing account data", e);
      return;
    } catch (Exception e) {
      log.error("could not store account data", e);
      return;
    } finally {
      IOUtils.closeQuietly(dataOut);
    }

    log.debug("saved " + accounts.size() + " account(s)");
  }

  private XStream createXStream() {
    XStream xStream = XStreamFactory.getSecureXStream();

    xStream.alias("accounts", AccountStoreInformation.class);
    xStream.alias("xmppAccount", XMPPAccount.class);
    return xStream;
  }

  /**
   * Returns a list containing all accounts.
   *
   * @return
   */
  public synchronized List<XMPPAccount> getAllAccounts() {
    return accounts
        .stream()
        .sorted(XMPPAccountStore::compare)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Returns a list of all used domains.
   *
   * <p><b>Example:</b><br>
   * If the {@link XMPPAccountStore} contains users
   *
   * <ul>
   *   <li>alice@jabber.org
   *   <li>bob@xyz.com [googlemail.com]
   *   <li>carl@saros-con.imp.fu-berlin.de
   * </ul>
   *
   * the server list contains
   *
   * <ul>
   *   <li>jabber.org
   *   <li>xyz.com
   *   <li>saros-con.imp.fu-berlin.de
   * </ul>
   *
   * @return
   */
  public List<String> getDomains() {
    final Set<String> domains = new HashSet<String>();

    for (final XMPPAccount account : getAllAccounts()) domains.add(account.getDomain());

    return new ArrayList<String>(domains);
  }

  /**
   * Returns a list of all used servers.
   *
   * <p><b>Example:</b><br>
   * If the {@link XMPPAccountStore} contains users
   *
   * <ul>
   *   <li>alice@jabber.org
   *   <li>bob@xyz.com [googlemail.com]
   *   <li>carl@saros-con.imp.fu-berlin.de
   * </ul>
   *
   * the server list contains
   *
   * <ul>
   *   <li>googlemail.com
   * </ul>
   *
   * @return
   */
  public synchronized List<String> getServers() {

    final Set<String> servers = new HashSet<String>();

    for (final XMPPAccount account : getAllAccounts()) servers.add(account.getServer());

    return new ArrayList<String>(servers);
  }

  /**
   * Sets the given account as the default one.
   *
   * @param account the account to set as default or <code>null</code>
   * @throws IllegalArgumentException if the account is not found in the store
   */
  public void setDefaultAccount(final XMPPAccount account) {
    synchronized (this) {
      if (account != null && !accounts.contains(account))
        throw new IllegalArgumentException(
            "account '" + account + "' is not in the current account store");

      defaultAccount = account;

      saveAccounts();
    }

    notifyActiveAccountListeners();
  }

  /**
   * Makes the given account active.
   *
   * @param account the account to activate
   * @throws IllegalArgumentException if the account is not found in the store
   * @deprecated Will be removed soon. Use {@link #setDefaultAccount(XMPPAccount)} instead.
   */
  @Deprecated
  public void setAccountActive(XMPPAccount account) {
    synchronized (this) {
      if (!accounts.contains(account))
        throw new IllegalArgumentException(
            "account '" + account + "' is not in the current account store");

      defaultAccount = account;

      saveAccounts();
    }

    notifyActiveAccountListeners();
  }

  /**
   * Deletes an account from the store. If this was the default account the default account is set
   * to <code>null</code>.
   *
   * @param account the account to delete
   * @throws IllegalArgumentException if the account is not found in the store
   */
  public void deleteAccount(final XMPPAccount account) {
    synchronized (this) {
      if (!accounts.remove(account))
        throw new IllegalArgumentException(
            "account '" + account + "' is not in the current account store");

      if (Objects.equals(defaultAccount, account)) defaultAccount = null;

      saveAccounts();
    }

    notifyAccountStoreListeners();
    if (defaultAccount == null) notifyActiveAccountListeners();
  }

  /**
   * Creates an account. The account will automatically become the default if the account store is
   * empty.
   *
   * @param username the user name of the new account as lower case string
   * @param password the password of the new account.
   * @param domain the domain name of the server
   * @param server the server of the new account as lower case string or an empty string if not used
   * @param port the port of the server or 0 if not used
   * @param useTLS if the connection should be secured using TLS
   * @param useSASL if the authentication should be negotiated using SASL
   * @throws NullPointerException if username, password, domain or server is null
   * @throws IllegalArgumentException if username or domain string is empty or only contains
   *     whitespace characters<br>
   *     if the domain or server contains upper case characters<br>
   *     if the port value is not in range of 0 < x <= 65535<br>
   *     if the server string is not empty and the port is 0<br>
   *     if an account already exists with the given username, password, domain, server and port
   */
  public XMPPAccount createAccount(
      String username,
      String password,
      String domain,
      String server,
      int port,
      boolean useTLS,
      boolean useSASL) {

    final XMPPAccount newAccount =
        new XMPPAccount(username, password, domain, server, port, useTLS, useSASL);

    synchronized (this) {
      if (accounts.contains(newAccount))
        throw new IllegalArgumentException("account " + newAccount + " already exists");

      if (accounts.isEmpty()) defaultAccount = newAccount;

      accounts.add(newAccount);

      saveAccounts();
    }

    notifyAccountStoreListeners();
    if (Objects.equals(defaultAccount, newAccount)) notifyActiveAccountListeners();

    return newAccount;
  }

  /**
   * Changes the properties of an account.
   *
   * @param account the existing account to be altered
   * @param username the new user name
   * @param password the new password
   * @param domain the domain name of the server
   * @param server the server ip / hostname
   * @param port the port of the server
   * @param useTLS if the connection should be secured using TLS
   * @param useSASL if the authentication should be negotiated using SASL
   * @throws IllegalArgumentException if username or domain string is empty or only contains
   *     whitespace characters<br>
   *     if the domain or server contains upper case characters<br>
   *     if the port value is not in range of 0 <= x <= 65535<br>
   *     if the server string is not empty and the port is 0<br>
   *     if an account already exists with the given username, password, domain, server and port
   */
  public void changeAccountData(
      XMPPAccount account,
      String username,
      String password,
      String domain,
      String server,
      int port,
      boolean useTLS,
      boolean useSASL) {

    XMPPAccount changedAccount =
        new XMPPAccount(username, password, domain, server, port, useTLS, useSASL);

    synchronized (this) {
      accounts.remove(account);

      if (accounts.contains(changedAccount)) {
        accounts.add(account);
        throw new IllegalArgumentException(
            "an account with user name '"
                + username
                + "', domain '"
                + domain
                + "' and server '"
                + server
                + "' with port '"
                + port
                + "' already exists");
      }

      account.setUsername(username);
      account.setPassword(password);
      account.setDomain(domain);
      account.setServer(server);
      account.setPort(port);
      account.setUseSASL(useSASL);
      account.setUseTLS(useTLS);

      accounts.add(account);

      saveAccounts();
    }

    notifyAccountStoreListeners();
  }

  /**
   * Returns the current default account.
   *
   * @return the default account or <code>null</code> if the default account is not set
   */
  public synchronized XMPPAccount getDefaultAccount() {
    return defaultAccount;
  }

  /**
   * Returns the current active account.
   *
   * @return the active account
   * @throws IllegalStateException if the account store is empty
   * @deprecated Will be removed soon. Use {@link #getDefaultAccount()} instead.
   */
  @Deprecated
  public synchronized XMPPAccount getActiveAccount() {
    if (defaultAccount != null) return defaultAccount;

    if (accounts.isEmpty()) throw new IllegalStateException("the account store is empty");

    // backward compatibility for now, just pick one
    setAccountActive(accounts.iterator().next());

    assert defaultAccount != null;

    return defaultAccount;
  }

  /**
   * Returns if the account store is currently empty
   *
   * @return <code>true</code> if the account store is empty, <code>false</code> otherwise
   */
  public synchronized boolean isEmpty() {
    return accounts.isEmpty();
  }

  /**
   * Checks if the an account with the given arguments exists in the account
   * store.
   *
   * @param username
   *            the username
   * @param domain
   *            the domain name of the server
   * @param server
   *            the server address / name
   * @param port
   *            the port of the server
   * @return <code>true if such an account exists, <code>false</code>
   *         otherwise
   */
  public synchronized boolean existsAccount(
      String username, String domain, String server, int port) {
    return getAllAccounts()
        .stream()
        .anyMatch(a -> matchesAccount(a, username, domain, server, port));
  }

  /**
   * Returns the account for the given username and domain.
   *
   * <p><b>Note:</b> If the store contains multiple accounts for the given username and domain (e.g
   * with different server address / name entries) the first account that matches will be returned.
   *
   * @param username the username to lookup
   * @param domain the domain to lookup
   * @return the account or <code>null</code> if the account does not exist
   */
  public XMPPAccount getAccount(final String username, final String domain) {
    return getAllAccounts()
        .stream()
        .filter(a -> matchesAnyServer(a, username, domain))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the account for the given username, domain, server address / name, and port.
   *
   * @param username the username to lookup
   * @param domain the domain to lookup
   * @param server the server/address to lookup
   * @param port the port to lookup
   * @return the account or <code>null</code> if the account does not exist
   */
  public XMPPAccount getAccount(
      final String username, final String domain, final String server, final int port) {
    return getAllAccounts()
        .stream()
        .filter(a -> matchesAccount(a, username, domain, server, port))
        .findFirst()
        .orElse(null);
  }

  /**
   * Searches for an account in the account store.
   *
   * @param jidString the jid of the user as string
   * @return the matching XMPP account or null in case of no match
   * @throws NullPointerException if jidString is null
   * @deprecated Will be removed soon. Use {@link #getAccount(String, String)} instead.
   */
  @Deprecated
  public XMPPAccount findAccount(String jidString) {
    if (jidString == null) {
      throw new NullPointerException("Null argument 'jidString'");
    }
    JID jid = new JID(jidString);
    String username = jid.getName();
    String domain = jid.getDomain();

    for (XMPPAccount account : getAllAccounts()) {
      if (domain.equalsIgnoreCase(account.getDomain())
          && username.equalsIgnoreCase(account.getUsername())) {
        return account;
      }
    }

    return null;
  }

  /**
   * class which is used for serialization of account information
   *
   * <p>WARNING: If you change this class you may change the XML format of the account file.
   */
  private static class AccountStoreInformation {
    public int activeAccountIndex;
    public ArrayList<XMPPAccount> configuredAccounts;

    public AccountStoreInformation(int activeAccountIndex, ArrayList<XMPPAccount> accounts) {
      this.configuredAccounts = accounts;
      this.activeAccountIndex = activeAccountIndex;
    }
  }

  /**
   * As Saros source code is open source both methods (and other) are not intended to produce a
   * secure encryption. We only do it to prevent foreign users stumbling across the account file to
   * easily gaining its contents (the password and account name). The only security we ensure is to
   * save the result in the user home directory which should be default only be accessible by the
   * user itself or administrators.
   */
  static class Crypto {
    private static final byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final int CHUNK_SIZE = 4096;

    private static final byte[] XOR_KEY = {
      115, 97, 114, 111, 115, 95, 120, 109, 112, 112, 95, 97, 99, 99, 95, 115, 116, 111, 114, 101,
      95, 120, 111, 114, 95, 107, 101, 121
    };

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
    private static final String SECRET_KEY_ALGORITHM = "AES";

    public static byte[] encrypt(byte[] data, String key)
        throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      MessageDigest digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
      digest.update(key.getBytes("UTF-8"));

      // default JVM impl. may only support key strength up to 128 bit;
      byte[] keyData = new byte[16];
      System.arraycopy(digest.digest(), 0, keyData, 0, keyData.length);

      SecretKeySpec keySpec = new SecretKeySpec(keyData, SECRET_KEY_ALGORITHM);
      IvParameterSpec ivSpec = new IvParameterSpec(IV);

      data = deflate(data);

      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

      return xor(cipher.doFinal(data));
    }

    public static byte[] decrypt(byte[] data, String key)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
            IOException {

      data = xor(data);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      MessageDigest digest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
      digest.update(key.getBytes("UTF-8"));

      // default JVM impl. may only support key strength up to 128;
      byte[] keyData = new byte[16];
      System.arraycopy(digest.digest(), 0, keyData, 0, keyData.length);

      SecretKeySpec keySpec = new SecretKeySpec(keyData, SECRET_KEY_ALGORITHM);
      IvParameterSpec ivSpec = new IvParameterSpec(IV);

      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

      return inflate(cipher.doFinal(data));
    }

    private static byte[] deflate(byte[] input) {

      Deflater compressor = new Deflater();
      compressor.setInput(input);
      compressor.finish();

      ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

      byte[] buf = new byte[CHUNK_SIZE];

      while (!compressor.finished()) {
        int count = compressor.deflate(buf);
        bos.write(buf, 0, count);
      }

      return bos.toByteArray();
    }

    private static byte[] inflate(byte[] input) throws IOException {

      ByteArrayOutputStream bos;
      Inflater decompressor = new Inflater();

      decompressor.setInput(input, 0, input.length);
      bos = new ByteArrayOutputStream(input.length);

      byte[] buf = new byte[CHUNK_SIZE];

      try {
        while (!decompressor.finished()) {
          int count = decompressor.inflate(buf);
          bos.write(buf, 0, count);
        }
        return bos.toByteArray();
      } catch (DataFormatException e) {
        throw new IOException("failed to inflate data", e);
      }
    }

    private static byte[] xor(byte data[]) {
      for (int i = 0, j = 0; i < data.length; i++) {
        data[i] ^= XOR_KEY[j++];

        if (j >= XOR_KEY.length) j = 0;
      }

      return data;
    }
  }

  private static int compare(final XMPPAccount a, XMPPAccount b) {
    int c = a.getUsername().compareToIgnoreCase(b.getUsername());

    if (c != 0) return c;

    c = a.getDomain().compareToIgnoreCase(b.getDomain());

    if (c != 0) return c;

    c = a.getServer().compareToIgnoreCase(b.getServer());

    if (c != 0) return c;

    return Integer.valueOf(a.getPort()).compareTo(Integer.valueOf(b.getPort()));
  }

  private static boolean matchesAnyServer(
      final XMPPAccount account, final String username, final String domain) {

    return account.getUsername().equals(username) && account.getDomain().equalsIgnoreCase(domain);
  }

  private static boolean matchesAccount(
      final XMPPAccount account,
      final String username,
      final String domain,
      final String server,
      final int port) {

    return account.getUsername().equals(username)
        && account.getDomain().equalsIgnoreCase(domain)
        && account.getServer().equalsIgnoreCase(server)
        && account.getPort() == port;
  }
}
