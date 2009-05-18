/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.util.HashMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.Jingle;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;

@Component(module = "net")
public class DiscoveryManager {
    HashMap<JID, Boolean> jingleSupport = new HashMap<JID, Boolean>();

    protected XMPPConnection connection;

    public DiscoveryManager(XMPPConnection connection) {
        this.connection = connection;
    }

    public boolean getCachedJingleSupport(JID recipient) {
        if (jingleSupport.containsKey(recipient)) {
            return jingleSupport.get(recipient);
        } else {
            boolean hasJingleSupport = queryJingleSupport(recipient);
            jingleSupport.put(recipient, hasJingleSupport);
            return hasJingleSupport;
        }
    }

    public boolean queryJingleSupport(JID recipient) {
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(connection);

        try {
            return sdm.discoverInfo(recipient.toString()).containsFeature(
                Jingle.NAMESPACE);
        } catch (XMPPException e) {
            return false;
        }
    }
}