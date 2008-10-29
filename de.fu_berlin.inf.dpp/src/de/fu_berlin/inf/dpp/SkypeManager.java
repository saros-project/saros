package de.fu_berlin.inf.dpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SkypeIQ;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype
 * and that allows to initiate Skype VOiP sessions with that entity.
 * 
 * @author rdjemili
 */
public class SkypeManager implements IConnectionListener {
    private static SkypeManager instance;

    private final Map<JID, String> skypeNames = new HashMap<JID, String>();

    private SkypeManager() {
	Saros.getDefault().addListener(this);
	ProviderManager providermanager = ProviderManager.getInstance();
	providermanager
		.addIQProvider("query", "jabber:iq:skype", SkypeIQ.class);
    }

    public static SkypeManager getDefault() {
	if (SkypeManager.instance == null) {
	    SkypeManager.instance = new SkypeManager();
	}

	return SkypeManager.instance;
    }

    /**
     * Returns the Skype-URL for given roster entry.
     * 
     * @return the skype url for given roster entry or <code>null</code> if
     *         roster entry has no skype name.
     */
    public String getSkypeURL(RosterEntry rosterEntry) {
	XMPPConnection connection = Saros.getDefault().getConnection();
	JID jid = new JID(rosterEntry.getUser());

	String name;
	if (this.skypeNames.containsKey(jid)) {
	    name = this.skypeNames.get(jid);

	} else {
	    name = SkypeManager.requestSkypeName(connection, jid);
	    this.skypeNames.put(jid, name);
	}

	return name == null ? null : "skype:" + name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.net.IConnectionListener
     */
    public void connectionStateChanged(final XMPPConnection connection,
	    ConnectionState newState) {

	if (newState == ConnectionState.CONNECTED) {
	    connection.addPacketListener(new PacketListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jivesoftware.smack.PacketListener
		 */
		public void processPacket(Packet packet) {
		    if (packet instanceof SkypeIQ) {
			SkypeIQ iq = (SkypeIQ) packet;

			SkypeIQ result = new SkypeIQ();
			result.setType(IQ.Type.RESULT);
			result.setPacketID(iq.getPacketID());
			result.setTo(iq.getFrom()); // HACK
			result.setName(getLocalSkypeName());

			connection.sendPacket(result);
		    }
		}
	    }, new IQTypeFilter(IQ.Type.GET));
	}
    }

    /**
     * @return the local skype name or <code>null</code> if none is set.
     */
    private String getLocalSkypeName() {
	IPreferenceStore prefs = Saros.getDefault().getPreferenceStore();
	return prefs.getString(PreferenceConstants.SKYPE_USERNAME);
    }

    /**
     * Requests the Skype user name of given user. This method blocks up to 5
     * seconds to receive the value.
     * 
     * @param connection
     * @param user
     *            the user for which the Skype name is requested.
     * @return the Skype user name of given user or <code>null</code> if the
     *         user doesn't respond in time or has no Skype name.
     */
    private static String requestSkypeName(XMPPConnection connection, JID user) {
	if ((connection == null) || !connection.isConnected()) {
	    return null;
	}

	// Request the time from a remote user.
	SkypeIQ request = new SkypeIQ();

	request.setType(IQ.Type.GET);
	request.setTo(user.toString() + "/Smack"); // HACK

	// Create a packet collector to listen for a response.
	PacketCollector collector = connection
		.createPacketCollector(new PacketIDFilter(request.getPacketID()));

	connection.sendPacket(request);

	// Wait up to 5 seconds for a result.
	IQ result = (IQ) collector.nextResult(5000);
	if ((result != null) && (result.getType() == IQ.Type.RESULT)) {
	    SkypeIQ skypeResult = (SkypeIQ) result;

	    return skypeResult.getName().length() == 0 ? null : skypeResult
		    .getName();
	}

	return null;
    }
}
