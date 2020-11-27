package saros.net.xmpp.subscription;

import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/**
 * This is class is responsible for handling XMPP subscriptions requests.
 *
 * <p>See also XMPP RFC 3921: http://xmpp.org/rfcs/rfc3921.html
 */
@Component(module = "net")
public class SubscriptionHandler {
  private static final Logger log = Logger.getLogger(SubscriptionHandler.class);

  private Connection connection = null;

  private final CopyOnWriteArrayList<SubscriptionListener> subscriptionListeners =
      new CopyOnWriteArrayList<SubscriptionListener>();

  private final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState connectionSate) {

          if (connectionSate == ConnectionState.CONNECTING) prepareConnection(connection);
          else if (connectionSate != ConnectionState.CONNECTED) disposeConnection();
        }
      };

  private final PacketListener packetListener =
      new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
          try {
            processPresence((Presence) packet);
          } catch (RuntimeException e) {
            log.error("failed to process packet: " + packet, e);
          }
        }
      };

  public SubscriptionHandler(XMPPConnectionService connectionService) {
    connectionService.addListener(connectionListener);
  }

  private synchronized void prepareConnection(Connection connection) {

    disposeConnection();

    this.connection = connection;
    connection.addPacketListener(packetListener, new PacketTypeFilter(Presence.class));
  }

  private synchronized void disposeConnection() {
    if (connection != null) connection.removePacketListener(packetListener);
  }

  /**
   * Changes the subscription state for the given contact to {@link Type#subscribed} allowing this
   * contact to see the online and offline status of the currently connected user.
   *
   * @param jid the {@linkplain JID} of the contact
   * @param requestSubscription if <code>true</code> the contact a subscription request to the given
   *     contact will be made
   * @return <code>true</code> if the operation was successful or <code>false</code> if an error
   *     occurred or not connected to an XMPP server
   */
  public synchronized boolean addSubscription(JID jid, boolean requestSubscription) {
    if (connection == null) return false;

    boolean success = sendSubscribedPresence(jid);

    if (requestSubscription) success &= requestSubscription(jid);

    return success;
  }

  /**
   * Changes the subscription state for the given contact to {@link Type#unsubscribed} disallowing
   * this contact to see the online and offline status of the currently connected user. This method
   * may also be called to reject a subscription request from a contact.
   *
   * @param jid the {@linkplain JID} of the contact
   * @return <code>true</code> if the operation was successful or <code>false</code> if an error
   *     occurred or not connected to an XMPP server
   */
  public synchronized boolean removeSubscription(JID jid) {
    if (connection == null) return false;

    return sendUnsubscribedPresence(jid);
  }

  /*
   * TODO the RFC states that we should send presence replies for subscribed
   * and unsubscribed presences ... check if this this handled correctly by
   * the server or we would create an infinite loop
   */
  private void processPresence(Presence presence) {
    if (presence.getFrom() == null) return;

    JID jid = new JID(presence.getFrom());

    switch (presence.getType()) {
      case error:
        String message =
            MessageFormat.format(
                "received error presence package from {0}, condition: {1}, message: {2}",
                presence.getFrom(),
                presence.getError().getCondition(),
                presence.getError().getMessage());
        log.warn(message);
        return;

      case subscribed:
        log.debug("contact subscribed to us: " + jid);
        break;

      case unsubscribed:
        log.debug("contact unsubscribed from us: " + jid);
        notifySubscriptionCanceled(jid);
        break;

      case subscribe:
        log.debug("contact requests to subscribe to us: " + jid);
        notifySubscriptionReceived(jid);
        break;

      case unsubscribe:
        log.debug("contact requests to unsubscribe from us: " + jid);
        removeSubscription(jid);
        break;

      default:
        // do nothing
    }
  }

  private synchronized boolean requestSubscription(JID jid) {
    boolean success = true;

    RosterEntry entry = connection.getRoster().getEntry(jid.getBase());

    try {
      if (entry != null) sendPresence(Presence.Type.subscribe, jid.getBase());
      else
        // will send a subscribe presence
        connection.getRoster().createEntry(jid.getBase(), null, null);

    } catch (XMPPException e) {
      log.error("adding user to roster failed", e);
      success = false;
    } catch (IllegalStateException e) {
      log.error("cannot add user to roster, not connected to a XMPP server", e);
      success = false;
    }

    return success;
  }

  private boolean sendSubscribedPresence(JID jid) {
    boolean success = true;

    try {
      sendPresence(Presence.Type.subscribed, jid.getBase());
    } catch (IllegalStateException e) {
      log.error("failed to send subscribe message, not connected to a XMPP server", e);

      success = false;
    }

    return success;
  }

  private boolean sendUnsubscribedPresence(JID jid) {
    boolean success = true;

    try {
      sendPresence(Presence.Type.unsubscribed, jid.getBase());
    } catch (IllegalStateException e) {
      log.error("failed to send unsubscribed message, not connected to a XMPP server", e);

      success = false;
    }

    return success;
  }

  private void sendPresence(Presence.Type type, String to) {
    Presence presence = new Presence(type);
    presence.setTo(to);
    presence.setFrom(connection.getUser());
    connection.sendPacket(presence);
  }

  /**
   * Adds a {@link SubscriptionListener}
   *
   * @param listener
   */
  public void addSubscriptionListener(SubscriptionListener listener) {
    subscriptionListeners.addIfAbsent(listener);
  }

  /**
   * Removes a {@link SubscriptionListener}
   *
   * @param listener
   */
  public void removeSubscriptionListener(SubscriptionListener listener) {
    subscriptionListeners.remove(listener);
  }

  /** Notify all {@link SubscriptionListener}s about an updated feature support. */
  private void notifySubscriptionReceived(final JID jid) {
    for (SubscriptionListener subscriptionManagerListener : subscriptionListeners)
      subscriptionManagerListener.subscriptionRequestReceived(jid);
  }

  /** Notify all {@link SubscriptionListener}s about an removed subscription. */
  private void notifySubscriptionCanceled(JID jid) {
    for (SubscriptionListener subscriptionManagerListener : subscriptionListeners)
      subscriptionManagerListener.subscriptionCanceled(jid);
  }
}
