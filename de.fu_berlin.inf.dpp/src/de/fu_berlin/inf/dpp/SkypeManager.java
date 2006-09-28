package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A manager class that allows to discover if a given XMPP entity supports Skype
 * and that allows to initiate Skype VOiP sessions with that entity.
 * 
 * @author rdjemili
 */
public class SkypeManager implements IConnectionListener {
    public static final String DISCO_NAMESPACE = "de.fu_berlin.inf.dpp/skype";
    public static final String DISCO_NODE      = "de.fu_berlin.inf.dpp/skype#name";

    private static SkypeManager instance;
    
    private SkypeManager() {
        Saros.getDefault().addListener(this);
    }
    
    public static SkypeManager getDefault() {
        if (instance == null)
            instance = new SkypeManager();
        
        return instance;
    }
    
    /**
     * Discovers the Skype name for given JID.
     * 
     * @param jid the user for which the Skype name should be discovered.
     * @return the skype name of given JID. Returns <code>null</code> if the
     * given JID doesn't support this feature.
     */
    public static String discoverSkypeName(XMPPConnection connection, JID jid) {
        ServiceDiscoveryManager discoManager = 
            ServiceDiscoveryManager.getInstanceFor(connection);
        
        try {
            DiscoverItems result = discoManager.discoverItems(jid.toString(), DISCO_NODE);
            DiscoverItems.Item item = (DiscoverItems.Item)result.getItems().next();
            return item.getEntityID();
            
        } catch (XMPPException e) {
            System.out.println(e);
            return null;
        }
    }
    
    /**
     * Discovers if the given JID supports Skype sessions.
     */
    public static boolean isServiceEnabled(XMPPConnection connection, JID jid) {
        try {
            DiscoverInfo result = getDiscoManager(connection).discoverInfo(jid.toString());
            return result.containsFeature(DISCO_NAMESPACE);
            
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the local skype name or <code>null</code> if none is set.
     */
    public static String getLocalSkypeName() {
        IPreferenceStore prefs = Saros.getDefault().getPreferenceStore();
        String skypeName = prefs.getString(PreferenceConstants.SKYPE_USERNAME);
        
        return skypeName.length() == 0 ? null : skypeName;
    }
    
    public String getSkypeURL(RosterEntry rosterEntry) {
        XMPPConnection connection = Saros.getDefault().getConnection();
        JID jid = new JID(rosterEntry.getUser());
        
        String skypeName = discoverSkypeName(connection, jid);
        return "skype:"+skypeName;
    }

    public void connectionStateChanged(XMPPConnection connection, 
        ConnectionState newState) {
        
        if (newState == ConnectionState.CONNECTED) {
            ServiceDiscoveryManager discoManager = 
                ServiceDiscoveryManager.getInstanceFor(connection);
            
            if (getLocalSkypeName() != null)
                enableFeature(discoManager);
        }
    }

    private static ServiceDiscoveryManager getDiscoManager(XMPPConnection connection) {
        return ServiceDiscoveryManager.getInstanceFor(connection);
    }

    private static void enableFeature(ServiceDiscoveryManager manager) {
        manager.addFeature(DISCO_NAMESPACE);
        
        manager.setNodeInformationProvider(
            DISCO_NODE,
            new NodeInformationProvider() {
                public Iterator getNodeItems() {
                    ArrayList answer = new ArrayList();
                    String skypeName = getLocalSkypeName();
                    answer.add(new DiscoverItems.Item(skypeName));
                    return answer.iterator();
                }
            }
        );
    }
}
