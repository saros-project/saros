/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.Jingle;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class is responsible for performing ServiceDiscovery for features such
 * as Jingle and Saros.
 */
@Component(module = "net")
public class DiscoveryManager {
    private static final Logger log = Logger.getLogger(DiscoveryManager.class
        .getName());

    protected HashMap<JID, Boolean> jingleCache = new HashMap<JID, Boolean>();

    @Inject
    protected Saros saros;

    /**
     * This method returns true if Jingle is available on the given JID.
     * 
     * @param recipient
     *            The JID must have a resource identifier (user@host/resource),
     *            otherwise you get a blame StackTrace in your logs.
     * 
     * @blocking
     * @nonReentrant
     */
    public boolean isJingleSupported(JID recipient) {
        if (jingleCache.containsKey(recipient)) {
            return jingleCache.get(recipient);
        } else {
            boolean hasJingleSupport = queryJingleSupport(recipient);
            jingleCache.put(recipient, hasJingleSupport);
            return hasJingleSupport;
        }
    }

    /**
     * Perform a ServiceDiscovery and check if {@link Jingle#NAMESPACE} is among
     * the features supported by the given recipient.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns.
     * @reentrant This method can be called concurrently.
     */
    protected boolean queryJingleSupport(JID recipient) {

        if (recipient.getResource().equals(""))
            log.warn("Resource missing: ", new StackTrace());

        if (!saros.isConnected())
            throw new IllegalStateException("Not Connected");

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(saros.getConnection());

        try {
            return sdm.discoverInfo(recipient.toString()).containsFeature(
                Jingle.NAMESPACE);
        } catch (XMPPException e) {
            return false;
        }
    }
}