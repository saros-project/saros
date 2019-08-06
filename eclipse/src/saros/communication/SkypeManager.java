package saros.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import saros.communication.extensions.SarosPacketExtension;
import saros.misc.xstream.XStreamExtensionProvider;
import saros.misc.xstream.XStreamExtensionProvider.XStreamIQPacket;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.roster.IRosterListener;
import saros.preferences.PreferenceConstants;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype and that allows to
 * initiate Skype VOIP sessions with that entity.
 *
 * <p>TODO CO: Verify that IQ Packets are the best way of doing this. Could we also use
 * ServiceDiscovery?
 */
/*
 * Hint: There is a race condition in this code, e.g someone requests the name while
 * we setup a new one the wrong name can be received in the end. However the issue is
 * likely not to happen at all so we ignore it.
 */
public class SkypeManager implements IConnectionListener {

  private static final Logger log = Logger.getLogger(SkypeManager.class);

  private static final XStreamExtensionProvider<String> skypeProvider =
      new XStreamExtensionProvider<String>(SarosPacketExtension.EXTENSION_NAMESPACE, "skypeInfo");

  private final Map<JID, String> skypeNames = new ConcurrentHashMap<JID, String>();

  private final XMPPConnectionService connectionService;
  private final IPreferenceStore preferenceStore;

  private PacketListener packetListener =
      new PacketListener() {
        @Override
        public void processPacket(Packet packet) {

          @SuppressWarnings("unchecked")
          final XStreamIQPacket<String> iq = (XStreamIQPacket<String>) packet;

          if (iq.getType() == IQ.Type.GET) {

            final IQ reply = skypeProvider.createIQ(getLocalSkypeName());
            reply.setType(IQ.Type.RESULT);
            reply.setPacketID(iq.getPacketID());
            reply.setTo(iq.getFrom());

            final Connection connection = connectionService.getConnection();

            if (connection == null) return;

            try {
              connection.sendPacket(reply);
            } catch (IllegalStateException e) {
              log.warn("failed to send IQ-RESULT reply to contact: " + iq.getFrom(), e);
            }

            return;
          }

          /* see hint for the class, maybe it is better to ignore result if there is already a name
           * present and only force updates on SET
           */
          if (iq.getType() == IQ.Type.SET || iq.getType() == IQ.Type.RESULT) {

            String skypeName = iq.getPayload();

            if (skypeName != null && skypeName.trim().isEmpty()) skypeName = null;

            if (skypeName != null) {
              skypeNames.put(new JID(iq.getFrom()), skypeName);
              log.debug("Skype Username for " + iq.getFrom() + " added: " + skypeName);
            } else {
              skypeNames.remove(new JID(iq.getFrom()));
              log.debug("Skype Username for " + iq.getFrom() + " removed");
            }
          }
        }
      };

  private final IRosterListener rosterListener =
      new IRosterListener() {
        @Override
        public void presenceChanged(Presence presence) {

          if (!presence.isAvailable()) return;

          // TODO this also fires for AWAY, DND presence changes
          final String from = presence.getFrom();

          if (from == null) return;

          requestSkypeName(new JID(from));
        }
      };

  public SkypeManager(XMPPConnectionService connectionService, IPreferenceStore preferenceStore) {
    this.connectionService = connectionService;
    this.preferenceStore = preferenceStore;
    this.connectionService.addListener(this);

    /** Register for our preference store, so we can be notified if the Skype user name changes. */
    preferenceStore.addPropertyChangeListener(
        new IPropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.SKYPE_USERNAME)) {
              publishSkypeIQ();
            }
          }
        });
  }

  /**
   * Returns the Skype name for user identified by the given JID.
   *
   * @return the skype name for given {@link JID} or <code>null</code> if the user has no skype name
   *     or it is not known yet
   */
  public String getSkypeName(final JID jid) {
    return skypeNames.get(jid);
  }

  /**
   * Send the current Skype user name to all our contacts that are currently available.
   *
   * <p>TODO SS only send to those, that we know use Saros.
   */
  private void publishSkypeIQ() {
    final Connection connection = connectionService.getConnection();

    if (connection == null) return;

    final Roster roster = connection.getRoster();

    if (roster == null) return;

    final String localSkypeName = getLocalSkypeName();

    for (RosterEntry rosterEntry : roster.getEntries()) {
      for (Presence presence : asIterable(roster.getPresences(rosterEntry.getUser()))) {

        if (!presence.isAvailable()) continue;

        final String toJid = presence.getFrom();

        final IQ result = skypeProvider.createIQ(localSkypeName);
        result.setType(IQ.Type.SET);
        result.setTo(toJid);

        try {
          connection.sendPacket(result);
        } catch (IllegalStateException e) {
          log.warn("failed to send IQ-SET request to contact: " + toJid, e);
          // we can abort here as the connection is closed
          return;
        }
      }
    }
  }

  /** Register a new PacketListener for intercepting SkypeIQ packets. */
  @Override
  public void connectionStateChanged(final Connection connection, ConnectionState newState) {
    if (newState == ConnectionState.CONNECTING) {
      connection.addPacketListener(packetListener, skypeProvider.getIQFilter());
      connection.getRoster().addRosterListener(rosterListener);
    }

    if (newState == ConnectionState.DISCONNECTING) {
      connection.getRoster().removeRosterListener(rosterListener);
      connection.removePacketListener(packetListener);

      if (newState == ConnectionState.NOT_CONNECTED) skypeNames.clear();
    }
  }

  /** @return the local Skype name or <code>null</code> if none is set. */
  private String getLocalSkypeName() {
    final String localSkypeName = preferenceStore.getString(PreferenceConstants.SKYPE_USERNAME);

    if (localSkypeName.trim().isEmpty()) return null;

    return localSkypeName;
  }

  /** Requests the Skype user name of given user. */
  private void requestSkypeName(JID rqJID) {

    final Connection connection = connectionService.getConnection();

    if (connection == null) return;

    final IQ request = skypeProvider.createIQ(null);

    request.setType(IQ.Type.GET);
    request.setTo(rqJID.toString());

    try {
      connection.sendPacket(request);
    } catch (IllegalStateException e) {
      log.warn("failed to send IQ-GET request to contact: " + rqJID, e);
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

  // https://docs.microsoft.com/en-us/skype-sdk/skypeuris/skypeuriapireference

  public static String getChatCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded + "?chat";
  }

  public static String getAudioCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded;
  }

  public static String getVideoCallUri(final String skypeName) {
    final String encoded = urlEncode(skypeName);
    return encoded == null ? null : "skype:" + encoded + "?call&video=true";
  }

  public static boolean isEchoService(final String skypeName) {
    return "echo123".equalsIgnoreCase(skypeName);
  }

  private static String urlEncode(final String value) {

    String result = null;

    try {
      result = URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {

      log.warn("failed to url encode data:" + value, e);
    }

    return result;
  }

  private static Boolean isAvailable = null;

  /**
   * Crude check to see if Skype is available on the current system. This method may return <code>
   * true</code> even if <b>NO</b> Skype installation can be found. Subsequent calls will always
   * return the same result unless a refresh is performed.
   *
   * @param refresh if <code>true</code> a Skype installation will be searched again
   * @return <code>true</code> if Skype may be installed, <code>false</code> if it is definitely not
   *     installed
   */
  public static synchronized boolean isSkypeAvailable(final boolean refresh) {

    if (!refresh && isAvailable != null) return isAvailable;

    isAvailable = true;

    if (!SystemUtils.IS_OS_WINDOWS) return isAvailable;

    final ProcessBuilder builder = new ProcessBuilder("powershell");

    builder.command().add("-NonInteractive");
    builder.command().add("-command");
    builder
        .command()
        .add("$result=Get-AppxPackage -Name Microsoft.SkypeApp; if(!$result) { exit 1 }");

    builder.redirectErrorStream(true);

    InputStream in = null;

    final Process p;

    try {
      p = builder.start();

      in = p.getInputStream();

      while (in.read() != -1) {
        // NOP
      }

      isAvailable = p.waitFor() == 0;

    } catch (IOException | InterruptedException e) {
      log.warn("failed to determine Skype installation", e);
      isAvailable = true;

      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
    } finally {
      IOUtils.closeQuietly(in);
    }

    return isAvailable;
  }
}
