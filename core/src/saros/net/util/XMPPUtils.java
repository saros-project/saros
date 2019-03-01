package saros.net.util;

import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.search.UserSearch;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/** Utility class for classic XMPP operations */
public class XMPPUtils {
  private static final Logger log = Logger.getLogger(XMPPUtils.class);

  private static volatile XMPPConnectionService defaultConnectionService;

  /**
   * Sets the default connection service that should be used when no connection is used for various
   * utility methods offered by this class.
   *
   * @param connectionService
   */
  public static void setDefaultConnectionService(XMPPConnectionService connectionService) {
    defaultConnectionService = connectionService;
  }

  private XMPPUtils() {
    // no public instantiation allowed
  }

  /**
   * @param connectionService network component that should be used to resolve the nickname or
   *     <code>null</code> to use the default one
   * @param jid the JID to resolve the nickname for
   * @param alternative nickname to return if no nickname is available, can be <code>null</code>
   * @return The nickname associated with the given JID in the current roster or the
   *     <tt>alternative</tt> representation if the current roster is not available or the nickname
   *     has not been set.
   */
  public static String getNickname(
      XMPPConnectionService connectionService, final JID jid, final String alternative) {

    if (connectionService == null) connectionService = defaultConnectionService;

    if (connectionService == null) return alternative;

    Connection connection = connectionService.getConnection();

    if (connection == null) return alternative;

    Roster roster = connection.getRoster();

    if (roster == null) return alternative;

    RosterEntry entry = roster.getEntry(jid.getBase());

    if (entry == null) return alternative;

    String nickName = entry.getName();

    if (nickName != null && nickName.trim().length() > 0) return nickName;

    return alternative;
  }

  public static String getDisplayableName(RosterEntry entry) {
    String nickName = entry.getName();
    if (nickName != null && nickName.trim().length() > 0) {
      return nickName.trim();
    }
    return entry.getUser();
  }

  /**
   * Creates the given account on the given XMPP server.
   *
   * @blocking
   * @param server the server on which to create the account
   * @param username for the new account
   * @param password for the new account
   * @return <code>null</code> if the account was registered, otherwise a {@link Registration
   *     description} is returned which may containing additional information on how to register an
   *     account on the given XMPP server or an error code
   * @see Registration#getError()
   * @throws XMPPException exception that occurs while registering
   */
  public static Registration createAccount(String server, String username, String password)
      throws XMPPException {

    Connection connection = new XMPPConnection(server);

    try {
      connection.connect();

      Registration registration = getRegistrationInfo(connection, username);

      /*
       * TODO registration cannot be null, can it?
       */
      if (registration != null) {

        // no in band registration
        if (registration.getError() != null) return registration;

        // already registered
        if (registration.getAttributes().containsKey("registered")) return registration;

        // redirect
        if (registration.getAttributes().size() == 1
            && registration.getAttributes().containsKey("instructions")) return registration;
      }

      AccountManager manager = connection.getAccountManager();
      manager.createAccount(username, password);
    } finally {
      connection.disconnect();
    }

    return null;
  }

  /**
   * Removes given contact from the {@link Roster}.
   *
   * @blocking
   * @param rosterEntry the contact that is to be removed
   * @throws XMPPException is thrown if no connection is established.
   */
  public static void removeFromRoster(Connection connection, RosterEntry rosterEntry)
      throws XMPPException {
    if (!connection.isConnected()) {
      throw new XMPPException("Not connected");
    }
    connection.getRoster().removeEntry(rosterEntry);
  }

  /**
   * Returns whether the given JID can be found on the server.
   *
   * @blocking
   * @param connection
   * @throws XMPPException if the service discovery failed
   */
  public static boolean isJIDonServer(Connection connection, JID jid, String resourceHint)
      throws XMPPException {

    if (isListedInUserDirectory(connection, jid)) return true;

    ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);

    boolean discovered = sdm.discoverInfo(jid.getRAW()).getIdentities().hasNext();

    if (!discovered && jid.isBareJID() && resourceHint != null && !resourceHint.isEmpty()) {
      discovered = sdm.discoverInfo(jid.getBase() + "/" + resourceHint).getIdentities().hasNext();
    }

    return discovered;
  }

  /**
   * Retrieve XMPP Registration information from a server.
   *
   * <p>This implementation reuses code from Smack but also sets the from element of the IQ-Packet
   * so that the server could reply with information that the account already exists as given by
   * XEP-0077.
   *
   * <p>To see what additional information can be queried from the registration object, refer to the
   * XEP directly:
   *
   * <p>http://xmpp.org/extensions/xep-0077.html
   */
  public static synchronized Registration getRegistrationInfo(
      Connection connection, String toRegister) throws XMPPException {
    Registration reg = new Registration();
    reg.setTo(connection.getServiceName());
    reg.setFrom(toRegister);
    PacketFilter filter =
        new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));
    PacketCollector collector = connection.createPacketCollector(filter);

    final IQ result;

    try {
      connection.sendPacket(reg);
      result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());

    } finally {
      collector.cancel();
    }

    if (result == null) {
      throw new XMPPException("No response from server.");
    } else if (result.getType() == IQ.Type.ERROR) {
      throw new XMPPException(result.getError());
    } else {
      return (Registration) result;
    }
  }

  /**
   * Returns the service for a user directory. The user directory can be used to perform search
   * queries.
   *
   * @param connection the current XMPP connection
   * @param service a service, normally the domain of a XMPP server
   * @return the service for the user directory or <code>null</code> if it could not be
   *     determined @See {@link UserSearch#getSearchForm(Connection, String)}
   */
  public static String getUserDirectoryService(Connection connection, String service) {

    ServiceDiscoveryManager manager = ServiceDiscoveryManager.getInstanceFor(connection);

    DiscoverItems items;

    try {
      items = manager.discoverItems(service);
    } catch (XMPPException e) {
      log.error("discovery for service '" + service + "' failed", e);
      return null;
    }

    Iterator<DiscoverItems.Item> iter = items.getItems();

    while (iter.hasNext()) {
      DiscoverItems.Item item = iter.next();

      try {
        DiscoverInfo info = manager.discoverInfo(item.getEntityID());

        if (!info.containsFeature("jabber:iq:search")) continue;

        Iterator<Identity> identities = info.getIdentities();

        while (identities.hasNext()) {
          Identity identity = identities.next();
          if ("user".equalsIgnoreCase(identity.getType())) {
            return item.getEntityID();
          }
        }

      } catch (XMPPException e) {
        log.warn("could not query identity: " + item.getEntityID(), e);
      }
    }

    iter = items.getItems();

    // make a good guess
    while (iter.hasNext()) {
      DiscoverItems.Item item = iter.next();

      String entityID = item.getEntityID();

      if (entityID == null) continue;

      if (entityID.startsWith("vjud.")
          || entityID.startsWith("search.")
          || entityID.startsWith("users.")
          || entityID.startsWith("jud.")
          || entityID.startsWith("id.")) return entityID;
    }

    return null;
  }

  /**
   * Returns the service for multiuser chat.
   *
   * @param connection the current XMPP connection
   * @param service a service, normally the domain of a XMPP server
   * @return the service for the multiuser chat or <code>null</code> if it could not be determined
   */
  public static String getMultiUserChatService(Connection connection, String service) {

    ServiceDiscoveryManager manager = ServiceDiscoveryManager.getInstanceFor(connection);

    DiscoverItems items;

    try {
      items = manager.discoverItems(service);
    } catch (XMPPException e) {
      log.error("discovery for service '" + service + "' failed", e);
      return null;
    }

    Iterator<DiscoverItems.Item> iter = items.getItems();
    while (iter.hasNext()) {
      DiscoverItems.Item item = iter.next();
      try {
        Iterator<Identity> identities = manager.discoverInfo(item.getEntityID()).getIdentities();
        while (identities.hasNext()) {
          Identity identity = identities.next();
          if ("text".equalsIgnoreCase(identity.getType())
              && "conference".equalsIgnoreCase(identity.getCategory())) {
            return item.getEntityID();
          }
        }
      } catch (XMPPException e) {
        log.warn("could not query identity: " + item.getEntityID(), e);
      }
    }

    return null;
  }

  /**
   * Tries to find the user in the user directory of the server. This method may fail for different
   * XMPP Server implementation, because the 'user' variable is not mandatory neither that it must
   * be named 'user'. Currently works with ejabberd XMPP servers.
   */

  // TODO: remove this method, add more logic and let the GUI handle search
  // stuff

  private static boolean isListedInUserDirectory(Connection connection, JID jid) {

    String userDirectoryService = null;

    try {
      userDirectoryService = getUserDirectoryService(connection, connection.getServiceName());

      if (userDirectoryService == null) return false;

      UserSearch search = new UserSearch();

      Form form = search.getSearchForm(connection, userDirectoryService);

      String userFieldVariable = null;

      for (Iterator<FormField> it = form.getFields(); it.hasNext(); ) {
        FormField formField = it.next();

        if ("user".equalsIgnoreCase(formField.getVariable())) {
          userFieldVariable = formField.getVariable();
          break;
        }
      }

      if (userFieldVariable == null) return false;

      Form answerForm = form.createAnswerForm();

      answerForm.setAnswer(userFieldVariable, jid.getName());

      ReportedData data = search.sendSearchForm(connection, answerForm, userDirectoryService);

      for (Iterator<Row> it = data.getRows(); it.hasNext(); ) {

        Row row = it.next();

        Iterator<String> vit = row.getValues("jid");

        if (vit == null) continue;

        while (vit.hasNext()) {
          JID returnedJID = new JID(vit.next());
          if (jid.equals(returnedJID)) return true;
        }
      }

      return false;
    } catch (XMPPException e) {
      log.error("searching in the user directory + '" + userDirectoryService + "' failed", e);
      return false;
    }
  }

  /**
   * Validates the given JID.<br>
   * See: <i>https://tools.ietf.org/html/rfc6122#section-2</i> for details.
   *
   * @param jid the JID to validate
   * @return <code>true</code> if the given JID is valid, <code>false</code> otherwise
   */
  /*
   * TODO check if Smack 4.X contains Stringprep methods and / or JID
   * validation methods so this one can be replaced and the GNU libidn library
   * can be removed
   */
  public static boolean validateJID(final JID jid) {

    // TODO length check

    // TODO try to detect IP4/6 addresses and check them for valid ranges

    final String localpart = jid.getName();
    final String domainpart = jid.getDomain();
    final String resourcepart = jid.getResource();

    if (localpart != null && !localpart.isEmpty()) {
      try {
        Stringprep.nodeprep(localpart);
      } catch (StringprepException e) {
        return false;
      }
    }

    if (domainpart != null && !domainpart.isEmpty()) {
      try {
        Stringprep.nameprep(domainpart);
      } catch (StringprepException e) {
        return false;
      }
    } else if (domainpart == null || domainpart.isEmpty()) return false;

    if (resourcepart != null && !resourcepart.isEmpty()) {
      try {
        Stringprep.nodeprep(resourcepart);
      } catch (StringprepException e) {
        return false;
      }
    }

    return true;
  }
}
