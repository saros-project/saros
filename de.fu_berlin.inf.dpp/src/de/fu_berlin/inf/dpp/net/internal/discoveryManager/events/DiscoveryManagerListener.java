package de.fu_berlin.inf.dpp.net.internal.discoveryManager.events;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;

/**
 * Listener for {@link DiscoveryManager} events
 */
public interface DiscoveryManagerListener {
    /**
     * Gets called whenever a {@link JID}'s feature support was updated.
     * 
     * @param jid
     *            {@link JID} who's feature support was updated
     * @param feature
     *            who's support was updated
     * @param isSupported
     *            new support
     */
    public void featureSupportUpdated(JID jid, String feature,
        boolean isSupported);
}
