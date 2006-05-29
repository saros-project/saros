/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;

import org.eclipse.core.resources.IProject;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.internal.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.internal.SharedProject;
import de.fu_berlin.inf.dpp.listeners.IConnectionListener;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.xmpp.JID;
import de.fu_berlin.inf.dpp.xmpp.XMPPChatTransmitter;


/**
 * The SessionManager is responsible for initiating new Saros sessions and for
 * reacting to invitiations.
 * 
 * @author rdjemili
 */
public class SessionManager implements IConnectionListener {
    private ISharedProject         sharedProject;
    private List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

    ITransmitter                   transmitter;

    public SessionManager() {
        Saros.getDefault().addListener(this);
    }
    
    // TODO rename to startHostedSession
    public void startSession(IProject project) throws XMPPException {
        if (!Saros.getDefault().isConnected()) {
            throw new XMPPException("No connection");
        }
        
        JID myJID = Saros.getDefault().getMyJID();
        sharedProject = new SharedProject(transmitter, project, myJID); // HACK
        
        for (ISessionListener listener : listeners) {
            listener.sessionStarted(sharedProject);
        }
    }
    
    // TODO rename to joinSession
    public ISharedProject createIncomingSharedProject(IProject project, JID host, 
        JID driver, List<JID> users) {
        
        sharedProject = new SharedProject(transmitter, project, 
            Saros.getDefault().getMyJID(), host, driver, users);
        
        for (ISessionListener listener : listeners) {
            listener.sessionStarted(sharedProject);
        }
        
        return sharedProject;
    }
    
    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     */
    public void leaveSession() {
        if (sharedProject.isHost()) {
            transmitter.sendCloseSessionMessage(sharedProject);
        } else {
            transmitter.sendLeaveMessage(sharedProject);
        }
        
        sessionClosed();
    }
    
    /**
     * Is triggered when the lesson is closed. The user should use #leaveSession
     * to close session and not this method directly.
     */
    public void sessionClosed() { // HACK
        if (sharedProject == null) {
            throw new IllegalStateException("No running session.");
        }
        
        ISharedProject closedProject = sharedProject;
        sharedProject = null;
        
        for (ISessionListener listener : listeners) {
            listener.sessionEnded(closedProject);
        }
    }

    /**
     * @return the active SharedProject object or <code>null</code> if there
     * is no active project.
     */
    public ISharedProject getSharedProject() {
        return sharedProject;
    }
    
    public void addSessionListener(ISessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeSessionListener(ISessionListener listener) {
        listeners.remove(listener);
    }
    
    public IIncomingInvitationProcess createIncomingInvitation(JID from, String description) {
        IIncomingInvitationProcess process = new IncomingInvitationProcess(
            transmitter, from, description);
        
        for (ISessionListener listener : listeners) {
            listener.invitationReceived(process);
        }
        
        return process;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection, ConnectionState newState) {
        if (newState == Saros.ConnectionState.CONNECTED) { // HACK
            transmitter = new XMPPChatTransmitter(connection); 
        } else {
            transmitter = null;
        }
    }
}
