package de.fu_berlin.inf.dpp.communication;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.SarosPacketExtension;
import de.fu_berlin.inf.dpp.misc.xstream.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.misc.xstream.XStreamExtensionProvider.XStreamIQPacket;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype and that allows to
 * initiate Skype VOIP sessions with that entity.
 *
 * <p>TODO CO: Verify that IQ Packets are the best way of doing this. It seems kind of hackisch.
 * Could we also use ServiceDiscovery?
 *
 * @author rdjemili
 * @author oezbek
 */
@Component(module = "net")
public class SkypeManager implements IConnectionListener {
  private final Logger log = Logger.getLogger(SkypeManager.class);

  protected XStreamExtensionProvider<String> skypeProvider =
      new XStreamExtensionProvider<String>(SarosPacketExtension.EXTENSION_NAMESPACE, "skypeInfo");

  protected final Map<JID, String> skypeNames = new HashMap<JID, String>();

  private final XMPPConnectionService connectionService;
  private final IPreferenceStore preferenceStore;

  private PacketListener packetListener =
      new PacketListener() {
        @Override
        public void processPacket(Packet packet) {

          @SuppressWarnings("unchecked")
          XStreamIQPacket<String> iq = (XStreamIQPacket<String>) packet;

          if (iq.getType() == IQ.Type.GET) {
            IQ reply = skypeProvider.createIQ(getLocalSkypeName());
            reply.setType(IQ.Type.RESULT);
            reply.setPacketID(iq.getPacketID());
            reply.setTo(iq.getFrom());

            connectionService.getConnection().sendPacket(reply);
          }
          if (iq.getType() == IQ.Type.SET) {
            String skypeName = iq.getPayload();
            if (skypeName != null && skypeName.length() > 0) {
              skypeNames.put(new JID(iq.getFrom()), skypeName);
              log.debug("Skype Username for " + iq.getFrom() + " added: " + skypeName);
            } else {
              skypeNames.remove(new JID(iq.getFrom()));
              log.debug("Skype Username for " + iq.getFrom() + " removed");
            }
          }
        }
      };

  public SkypeManager(XMPPConnectionService connectionService, IPreferenceStore preferenceStore) {
    this.connectionService = connectionService;
    this.preferenceStore = preferenceStore;
    this.connectionService.addListener(this);

    /** Register for our preference store, so we can be notified if the Skype Username changes. */
    preferenceStore.addPropertyChangeListener(
        new IPropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.SKYPE_USERNAME)) {
              publishSkypeIQ(event.getNewValue().toString());
            }
          }
        });
  }

  /**
   * Non-blocking variant of {@link #getSkypeURL(String)}.
   *
   * @return the skype url for given roster entry or <code>null</code> if roster entry has no skype
   *     name or the entry's skype name is not cached
   *     <p>This method will return previously cached results.
   */
  public String getSkypeURLNonBlock(String jid) {

    Connection connection = connectionService.getConnection();
    if (connection == null) return null;

    Roster roster = connection.getRoster();
    if (roster == null) return null;

    for (Presence presence : asIterable(roster.getPresences(jid))) {
      if (presence.isAvailable()) {
        String result = getSkypeURLNonBlock(new JID(presence.getFrom()));
        if (result != null) return result;
      }
    }

    return null;
  }

  /**
   * Non-blocking variant of {@link #getSkypeURL(JID)}.
   *
   * @return the skype url for given {@link JID} or <code>null</code> if roster entry has no skype
   *     name or the entry's skype name is not cached
   *     <p>This method will return previously cached results.
   */
  public String getSkypeURLNonBlock(final JID rqJID) {
    if (this.skypeNames.containsKey(rqJID)) {
      return this.skypeNames.get(rqJID);
    }

    ThreadUtils.runSafeAsync(
        "dpp-skype-url-resolver",
        log,
        new Runnable() {
          @Override
          public void run() {
            getSkypeURL(rqJID);
          }
        });

    return null;
  }

  /**
   * Returns the Skype-URL for given JID. This method will query all presences associated with the
   * given JID and return the first valid Skype url (if any).
   *
   * <p>The order in which the presences are queried is undefined. If you need more control use
   * getSkypeURL(JID).
   *
   * @return the skype url for given roster entry or <code>null</code> if roster entry has no skype
   *     name.
   *     <p>This method will return previously cached results.
   * @blocking This method is potentially long-running
   */
  public String getSkypeURL(String jid) {

    Connection connection = connectionService.getConnection();
    if (connection == null) return null;

    Roster roster = connection.getRoster();
    if (roster == null) return null;

    for (Presence presence : asIterable(roster.getPresences(jid))) {
      if (presence.isAvailable()) {
        String result = getSkypeURL(new JID(presence.getFrom()));
        if (result != null) return result;
      }
    }

    return null;
  }

  /**
   * Returns the Skype-URL for user identified by the given RQ-JID.
   *
   * @return the skype url for given {@link JID} or <code>null</code> if the user has no skype name
   *     set for this client.
   *     <p>This method will return previously cached results.
   * @blocking This method is potentially long-running
   */
  public String getSkypeURL(JID rqJID) {

    Connection connection = connectionService.getConnection();

    if (connection == null) return null;

    String skypeName;

    if (this.skypeNames.containsKey(rqJID)) {
      skypeName = this.skypeNames.get(rqJID);
    } else {
      skypeName = requestSkypeName(connection, rqJID);
      if (skypeName != null) {
        // Only cache if we found something
        this.skypeNames.put(rqJID, skypeName);
      }
    }

    if (skypeName == null) return null;

    return "skype:" + skypeName;
  }

  /**
   * Send the given Skype user name to all our contacts that are currently available.
   *
   * <p>TODO SS only send to those, that we know use Saros.
   */
  public void publishSkypeIQ(String newSkypeName) {
    Connection connection = connectionService.getConnection();

    if (connection == null) return;

    Roster roster = connection.getRoster();
    if (roster == null) return;

    for (RosterEntry rosterEntry : roster.getEntries()) {
      for (Presence presence : asIterable(roster.getPresences(rosterEntry.getUser()))) {
        if (presence.isAvailable()) {
          IQ result = skypeProvider.createIQ(newSkypeName);
          result.setType(IQ.Type.SET);
          result.setTo(presence.getFrom());
          connection.sendPacket(result);
        }
      }
    }
  }

  /** Register a new PacketListener for intercepting SkypeIQ packets. */
  @Override
  public void connectionStateChanged(final Connection connection, ConnectionState newState) {
    if (newState == ConnectionState.CONNECTED) {
      connection.addPacketListener(packetListener, skypeProvider.getIQFilter());

      String skypeUsername = preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);
      if (skypeUsername != null && !skypeUsername.isEmpty()) publishSkypeIQ(skypeUsername);
      refreshCache(connection);
    }

    if (newState == ConnectionState.DISCONNECTING) {
      connection.removePacketListener(packetListener);
      skypeNames.clear();
    }
  }

  /**
   * Request the skype name of all known contacts and caches the results.
   *
   * @param connection
   */
  protected void refreshCache(final Connection connection) {
    log.debug("Refreshing Skype username cache...");
    for (final RosterEntry rosterEntry : connection.getRoster().getEntries()) {
      getSkypeURLNonBlock(rosterEntry.getUser());
    }
  }

  /** @return the local Skype name or <code>null</code> if none is set. */
  protected String getLocalSkypeName() {
    return preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);
  }

  /**
   * Requests the Skype user name of given user. This method blocks up to 5 seconds to receive the
   * value.
   *
   * @param rqJID the rqJID of the user for which the Skype name is requested.
   * @return the Skype user name of given user or <code>null</code> if the user doesn't respond in
   *     time (5s) or has no Skype name.
   */
  protected String requestSkypeName(Connection connection, JID rqJID) {

    if ((connection == null) || !connection.isConnected()) {
      return null;
    }

    IQ request = skypeProvider.createIQ(null);

    request.setType(IQ.Type.GET);
    request.setTo(rqJID.toString());

    // Create a packet collector to listen for a response.
    PacketCollector collector =
        connection.createPacketCollector(new PacketIDFilter(request.getPacketID()));

    try {
      connection.sendPacket(request);

      // Wait up to 5 seconds for a result.
      String skypeName = skypeProvider.getPayload(collector.nextResult(5000));

      if (skypeName == null || skypeName.trim().length() == 0) return null;
      else return skypeName.trim();
    } finally {
      collector.cancel();
    }
  }

  private static <T> Iterable<T> asIterable(final Iterator<T> it) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return it;
      }
    };
  }
}
