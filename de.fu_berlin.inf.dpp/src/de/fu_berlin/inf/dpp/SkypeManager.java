package de.fu_berlin.inf.dpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SkypeIQ;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype
 * and that allows to initiate Skype VOIP sessions with that entity.
 * 
 * TODO CO: Verify that IQ Packets are the best way of doing this. It seems kind
 * of hackisch. Could we also use ServiceDiscovery?
 * 
 * @author rdjemili
 * @author oezbek
 * 
 * @picocontainer This component is managed by the PicoContainer in
 *                {@link Saros}
 */
public class SkypeManager implements IConnectionListener {

    protected final Map<JID, String> skypeNames = new HashMap<JID, String>();

    protected Saros saros;

    public SkypeManager(Saros saros) {
        this.saros = saros;
        saros.addListener(this);
        ProviderManager providermanager = ProviderManager.getInstance();
        providermanager
            .addIQProvider("query", "jabber:iq:skype", SkypeIQ.class);

        /**
         * Register for our preference store, so we can be notified if the Skype
         * Username changes.
         */
        IPreferenceStore prefs = saros.getPreferenceStore();
        prefs.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
                    PreferenceConstants.SKYPE_USERNAME)) {
                    publishSkypeIQ(event.getNewValue().toString());
                }
            }
        });

    }

    /**
     * Returns the Skype-URL for given roster entry.
     * 
     * @return the skype url for given roster entry or <code>null</code> if
     *         roster entry has no skype name.
     * 
     * @blocking This method is potentially long-running
     */
    public String getSkypeURL(RosterEntry rosterEntry) {
        XMPPConnection connection = saros.getConnection();
        JID jid = new JID(rosterEntry.getUser());

        String name;
        if (this.skypeNames.containsKey(jid)) {
            name = this.skypeNames.get(jid);
        } else {
            name = SkypeManager.requestSkypeName(connection, jid);
            if (name != null) {
                // Only cache if we found something
                this.skypeNames.put(jid, name);
            }
        }

        return name == null ? null : "skype:" + name;
    }

    /**
     * Send the given Skype user name to all our contacts that are currently
     * available.
     * 
     * TODO SS only send to those, that we know use Saros.
     */
    public void publishSkypeIQ(String newSkypeName) {
        XMPPConnection connection = Saros.getDefault().getConnection();
        if (connection == null)
            return;

        Roster roster = connection.getRoster();
        if (roster == null)
            return;

        for (RosterEntry entry : roster.getEntries()) {
            String username = entry.getUser();
            Presence presence = roster.getPresence(username);

            if (presence != null && presence.isAvailable()) {
                SkypeIQ result = new SkypeIQ();
                result.setType(IQ.Type.SET);
                result.setTo(username + "/Smack");
                result.setName(newSkypeName);
                connection.sendPacket(result);
            }
        }
    }

    /**
     * Register a new PacketListener for intercepting SkypeIQ packets.
     */
    public void connectionStateChanged(final XMPPConnection connection,
        ConnectionState newState) {

        if (newState == ConnectionState.CONNECTED) {
            connection.addPacketListener(new PacketListener() {
                public void processPacket(Packet packet) {
                    SkypeIQ iq = (SkypeIQ) packet;

                    if (iq.getType() == IQ.Type.GET) {
                        SkypeIQ result = new SkypeIQ();
                        result.setType(IQ.Type.RESULT);
                        result.setPacketID(iq.getPacketID());
                        // HACK because we have rigged the JID to evade
                        // detection by Smack
                        result.setTo(iq.getFrom());
                        result.setName(getLocalSkypeName());

                        connection.sendPacket(result);
                    }
                    if (iq.getType() == IQ.Type.SET) {
                        String skypeName = iq.getName();
                        if (skypeName != null && skypeName.length() > 0) {
                            skypeNames.put(new JID(iq.getFrom()), skypeName);
                        } else {
                            skypeNames.remove(new JID(iq.getFrom()));
                        }
                    }
                }
            }, new PacketTypeFilter(SkypeIQ.class));
        } else {
            // Otherwise clear our cache
            skypeNames.clear();
        }
    }

    /**
     * @return the local Skype name or <code>null</code> if none is set.
     */
    protected String getLocalSkypeName() {
        IPreferenceStore prefs = saros.getPreferenceStore();
        return prefs.getString(PreferenceConstants.SKYPE_USERNAME);
    }

    /**
     * Requests the Skype user name of given user. This method blocks up to 5
     * seconds to receive the value.
     * 
     * @param user
     *            the user for which the Skype name is requested.
     * @return the Skype user name of given user or <code>null</code> if the
     *         user doesn't respond in time (5s) or has no Skype name.
     */
    protected static String requestSkypeName(XMPPConnection connection, JID user) {

        if ((connection == null) || !connection.isConnected()) {
            return null;
        }

        // Request the time from a remote user.
        SkypeIQ request = new SkypeIQ();

        request.setType(IQ.Type.GET);
        // HACK because Smack does not support IQ.Type.SET
        request.setTo(user.toString() + "/Smack");

        // Create a packet collector to listen for a response.
        PacketCollector collector = connection
            .createPacketCollector(new PacketIDFilter(request.getPacketID()));

        try {
            connection.sendPacket(request);

            // Wait up to 5 seconds for a result.
            IQ result = (IQ) collector.nextResult(5000);
            if ((result != null) && (result.getType() == IQ.Type.RESULT)) {
                SkypeIQ skypeResult = (SkypeIQ) result;

                return skypeResult.getName().length() == 0 ? null : skypeResult
                    .getName();
            }
        } finally {
            collector.cancel();
        }

        return null;
    }
}
