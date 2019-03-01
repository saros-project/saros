package saros.account;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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
 *
 * @author Stefan Rossbach
 */
@Component(module = "account")
public final class XMPPAccountStore {
  private static final Logger LOG = Logger.getLogger(XMPPAccountStore.class);

  private static final int MAX_ACCOUNT_DATA_SIZE = 10 * 1024 * 1024;

  private static final String DEFAULT_SECRET_KEY = "Saros";

  private final CopyOnWriteArrayList<IAccountStoreListener> listeners =
      new CopyOnWriteArrayList<IAccountStoreListener>();

  private Set<XMPPAccount> accounts;
  private XMPPAccount activeAccount;

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
    XMPPAccount active = !isEmpty() ? getActiveAccount() : null;
    for (IAccountStoreListener listener : listeners) {
      listener.activeAccountChanged(active);
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

      if (accountFile != null) {
        File parent = accountFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
          LOG.error("could not create directories for file: " + file.getAbsolutePath());
          accountFile = null;
        }
      }

      loadAccounts();
    }

    notifyAccountStoreListeners();
    notifyActiveAccountListeners();
  }

  @SuppressWarnings("unchecked")
  private synchronized void loadAccounts() {

    if (accountFile == null || !accountFile.exists()) return;

    if (accountFile.length() == 0) return;

    LOG.debug("loading accounts from file: " + accountFile.getAbsolutePath());

    DataInputStream dataIn = null;

    int size;

    byte[] buffer;

    try {
      dataIn = new DataInputStream(new FileInputStream(accountFile));

      size = dataIn.readInt();

      if (size <= 0 || size > MAX_ACCOUNT_DATA_SIZE)
        throw new IOException(
            "account data seems malformed, refused to load " + size + " bytes into memory");

      buffer = new byte[size];

      dataIn.readFully(buffer);

      activeAccount =
          (XMPPAccount)
              new ObjectInputStream(new ByteArrayInputStream(Crypto.decrypt(buffer, secretKey)))
                  .readObject();

      size = dataIn.readInt();

      if (size <= 0 || size > MAX_ACCOUNT_DATA_SIZE)
        throw new IOException(
            "account data seems malformed, refused to load " + size + " bytes into memory");

      buffer = new byte[size];

      dataIn.readFully(buffer);

      accounts =
          (Set<XMPPAccount>)
              new ObjectInputStream(new ByteArrayInputStream(Crypto.decrypt(buffer, secretKey)))
                  .readObject();

    } catch (RuntimeException e) {
      LOG.error("internal error while loading account data", e);
      return;
    } catch (Exception e) {
      LOG.error("could not load account data", e);
      return;
    } finally {
      IOUtils.closeQuietly(dataIn);
    }

    /*
     * remove us first and re add us, otherwise the active account object is
     * not in the set and the wrong object will be updated
     */
    accounts.remove(activeAccount);
    accounts.add(activeAccount);

    LOG.debug("loaded " + accounts.size() + " account(s)");
  }

  private synchronized void saveAccounts() {

    if (accountFile == null) return;

    if (!accountFile.exists()) {
      try {
        accountFile.createNewFile();
      } catch (IOException e) {
        LOG.error(
            "could not create file: " + accountFile.getAbsolutePath() + " to store account data");
        return;
      }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream oos;

    DataOutputStream dataOut = null;

    try {
      dataOut = new DataOutputStream(new FileOutputStream(accountFile));

      oos = new ObjectOutputStream(out);
      oos.writeObject(activeAccount);
      oos.flush();

      byte[] activeAccount = Crypto.encrypt(out.toByteArray(), secretKey);
      out.reset();

      oos = new ObjectOutputStream(out);
      oos.writeObject(accounts);
      oos.flush();

      byte[] allAccounts = Crypto.encrypt(out.toByteArray(), secretKey);

      dataOut.writeInt(activeAccount.length);
      dataOut.write(activeAccount);
      dataOut.writeInt(allAccounts.length);
      dataOut.write(allAccounts);

      dataOut.flush();

    } catch (RuntimeException e) {
      LOG.error("internal error while storing account data", e);
      return;
    } catch (Exception e) {
      LOG.error("could not store account data", e);
      return;
    } finally {
      IOUtils.closeQuietly(dataOut);
    }

    LOG.debug("saved " + accounts.size() + " account(s)");
  }

  /**
   * Returns a list containing all accounts.
   *
   * @return
   */
  public synchronized List<XMPPAccount> getAllAccounts() {
    List<XMPPAccount> accounts = new ArrayList<XMPPAccount>(this.accounts);

    Comparator<XMPPAccount> comparator =
        new Comparator<XMPPAccount>() {

          @Override
          public int compare(XMPPAccount a, XMPPAccount b) {
            int c = a.getUsername().compareToIgnoreCase(b.getUsername());

            if (c != 0) return c;

            c = a.getDomain().compareToIgnoreCase(b.getDomain());

            if (c != 0) return c;

            c = a.getServer().compareToIgnoreCase(b.getServer());

            if (c != 0) return c;

            return Integer.valueOf(a.getPort()).compareTo(Integer.valueOf(b.getPort()));
          }
        };

    Collections.sort(accounts, comparator);
    return accounts;
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
  public synchronized List<String> getDomains() {
    List<String> domains = new ArrayList<String>();
    for (XMPPAccount account : accounts) {
      String domain = account.getDomain();
      if (!domains.contains(domain)) domains.add(domain);
    }
    return domains;
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
   *   <li>jabber.org
   *   <li>googlemail.com
   *   <li>saros-con.imp.fu-berlin.de
   * </ul>
   *
   * @return
   */
  public synchronized List<String> getServers() {
    List<String> servers = new ArrayList<String>();
    for (XMPPAccount account : accounts) {
      String server = account.getServer();
      if (!servers.contains(server)) servers.add(server);
    }
    return servers;
  }

  /**
   * Makes the given account active.
   *
   * @param account the account to activate
   * @throws IllegalArgumentException if the account is not found in the store
   */
  public void setAccountActive(XMPPAccount account) {
    synchronized (this) {
      if (!accounts.contains(account))
        throw new IllegalArgumentException(
            "account '" + account + "' is not in the current account store");

      activeAccount = account;

      saveAccounts();
    }

    notifyActiveAccountListeners();
  }

  /**
   * Deletes an account.
   *
   * @param account the account to delete
   * @throws IllegalArgumentException if the account is not found in the store
   * @throws IllegalStateException if the account is active
   */
  public void deleteAccount(XMPPAccount account) {
    synchronized (this) {
      if (!accounts.contains(account))
        throw new IllegalArgumentException(
            "account '" + account + "' is not in the current account store");

      if (this.activeAccount == account)
        throw new IllegalStateException(
            "account '" + account + "' is active and cannot be deleted");

      accounts.remove(account);

      saveAccounts();
    }

    notifyAccountStoreListeners();
  }

  /**
   * Creates an account. The account will automatically become active if the account store is empty.
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

    XMPPAccount newAccount =
        new XMPPAccount(username, password, domain, server, port, useTLS, useSASL);

    synchronized (this) {
      if (accounts.contains(newAccount))
        throw new IllegalArgumentException("account already exists");

      if (accounts.isEmpty()) this.activeAccount = newAccount;

      this.accounts.add(newAccount);

      saveAccounts();
    }

    notifyAccountStoreListeners();

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
   * Returns the current active account.
   *
   * @return the active account
   * @throws IllegalStateException if the account store is empty
   */
  public synchronized XMPPAccount getActiveAccount() {
    if (activeAccount == null) throw new IllegalStateException("the account store is empty");

    return activeAccount;
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
   * store
   *
   * @param username
   *            the username
   * @param domain
   *            the domain name of the server
   * @param server
   *            the server ip / name
   * @param port
   *            the port of the server
   * @return <code>true if such an account exists, <code>false</code>
   *         otherwise
   */
  public synchronized boolean exists(String username, String domain, String server, int port) {
    for (XMPPAccount a : getAllAccounts()) {
      if (a.getServer().equalsIgnoreCase(server)
          && a.getDomain().equalsIgnoreCase(domain)
          && a.getUsername().equals(username)
          && a.getPort() == port) {
        return true;
      }
    }
    return false;
  }

  /**
   * Searches for an account in the account store.
   *
   * @param jidString the jid of the user as string
   * @return the matching XMPP account or null in case of no match
   * @throws NullPointerException if jidString is null
   */
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
   * As Saros source code is open source both methods (and other) are not intended to produce a
   * secure encryption. We only do it to prevent foreign users stumbling across the account file to
   * easily gaining its contents (the password and account name). The only security we ensure is to
   * save the result in the user home directory which should be default only be accessible by the
   * user itself or administrators.
   */
  private static class Crypto {
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
}
