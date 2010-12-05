package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.limewire.collection.Tuple;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.StateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponentImp;

public class SarosStateImp extends StateImp implements SarosState {

    private transient static final Logger log = Logger
        .getLogger(SarosStateImp.class);

    private static transient SarosStateImp self;

    /**
     * {@link ChatViewComponentImp} is a singleton, but inheritance is possible.
     */
    public static SarosStateImp getInstance() {
        if (self != null)
            return self;
        self = new SarosStateImp();
        return self;
    }

    /**********************************************
     * 
     * gather state and perform actions using {@link SarosSessionManager}
     * 
     * 
     **********************************************/

    public boolean isDriver() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.isDriver();
    }

    public boolean isDriver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getDrivers().contains(user));
        return sarosSession.getDrivers().contains(user);
    }

    public boolean isExclusiveDriver() throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            return sarosSession.isExclusiveDriver();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areDrivers(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getDrivers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isHost() throws RemoteException {
        return isHost(getJID());
    }

    public boolean isHost(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        final boolean result = user == sarosSession.getHost();
        log.debug("isHost(" + jid.toString() + ") == " + result);
        return result;
    }

    public boolean isObserver() throws RemoteException {
        return isObserver(getJID());
    }

    public boolean isObserver(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isObserver(" + jid.toString() + ") == "
            + sarosSession.getObservers().contains(user));
        return sarosSession.getObservers().contains(user);
    }

    public boolean areObservers(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getObservers().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isParticipant() throws RemoteException {
        return isParticipant(getJID());
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            User user = sarosSession.getUser(jid);
            if (user == null)
                return false;
            log.debug("isParticipant(" + jid.toString() + ") == "
                + sarosSession.getParticipants().contains(user));
            return sarosSession.getParticipants().contains(user);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areParticipants(List<JID> jids) throws RemoteException {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isInSession() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    /**********************************************
     * 
     * gather state and perform actions using {@link DataTransferManager}
     * 
     **********************************************/
    private Tuple<NetTransferMode, NetTransferMode> getConnection(JID destJid) {
        NetTransferMode outgoingMode = dataTransferManager
            .getTransferMode(destJid);
        NetTransferMode incomingMode = dataTransferManager
            .getTransferMode(destJid);
        return new Tuple<NetTransferMode, NetTransferMode>(incomingMode,
            outgoingMode);
    }

    public boolean isIncomingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isIncomingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isIncomingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isIncomingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    public boolean isOutgoingConnectionIBB(JID destJid) throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.IBB);
    }

    public boolean isOutgoingConnectionJingleTCP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLETCP);
    }

    public boolean isOutgoingConnectionJingleUDP(JID destJid)
        throws RemoteException {
        return dataTransferManager.getTransferMode(destJid).equals(
            NetTransferMode.JINGLEUDP);
    }

    public boolean isOutgoingConnectionSocks5ByteStream(JID destJid)
        throws RemoteException {
        throw new NotImplementedException(
            "We can not get NetTransferMode Socks5ByteStream connection in Saros yet.");
    }

    /**********************************************
     * 
     * gather state and perform actions using {@link Saros}
     * 
     **********************************************/

    public String getTestFileContents(String testFilePath)
        throws RemoteException {
        Bundle bundle = saros.getBundle();
        String contents;
        try {
            contents = FileUtils.read(bundle.getEntry(testFilePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + testFilePath);
        }
        return contents;
    }

    public String getPathToScreenShot() throws RemoteException {
        Bundle bundle = saros.getBundle();
        return bundle.getLocation().substring(16) + SCREENSHOTDIR;
    }

    /**********************************************
     * 
     * gather state and perform actions using {@link EditorManager}
     * 
     **********************************************/
    public boolean isInFollowMode() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isFollowingUser(String baseJID) throws RemoteException {
        if (getFollowedUserJID() == null)
            return false;
        else
            return getFollowedUserJID().getBase().equals(baseJID);
    }

    public JID getFollowedUserJID() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    /**********************************************
     * 
     * infos about local user
     * 
     **********************************************/
    public void setJID(JID jid) throws RemoteException {
        this.localJID = jid;
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    public boolean isSameUser(JID otherJID) throws RemoteException {
        return this.localJID.equals(otherJID);
    }
}
