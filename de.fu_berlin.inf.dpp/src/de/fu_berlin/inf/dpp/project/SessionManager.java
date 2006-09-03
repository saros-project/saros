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
package de.fu_berlin.inf.dpp.project;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.internal.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;


/**
 * The SessionManager is responsible for initiating new Saros sessions and for
 * reacting to invitiations. The user can be only part of one session at most.
 * 
 * @author rdjemili
 */
public class SessionManager implements IConnectionListener {
    private static Logger log = 
        Logger.getLogger(SessionManager.class.getName());
    
    private SharedProject         sharedProject;
//  TODO use ListenerList instead
    private List<ISessionListener> listeners = 
        new CopyOnWriteArrayList<ISessionListener>();
    
    ITransmitter                   transmitter;

    public SessionManager() {
        Saros.getDefault().addListener(this);
    }
    
    /**
     * Starts a new shared project with the local user as only participant.
     * 
     * @param project the local Eclipse project which should become shared.
     * @throws XMPPException if this method is called with no established
     * XMPP-connection.
     */
    public void startSession(IProject project) throws XMPPException {
        if (!Saros.getDefault().isConnected()) {
            throw new XMPPException("No connection");
        }
        
        JID myJID = Saros.getDefault().getMyJID();
        sharedProject = new SharedProject(transmitter, project, myJID);
        sharedProject.start();
        
//        sharedProject.getActivityManager().addProvider(provider)
        
        for (ISessionListener listener : listeners) {
            listener.sessionStarted(sharedProject);
        }
        
        log.info("Session started");
    }
    
    /**
     * Joins an remotly already running shared project.
     * 
     * @param project the local Eclipse project which should be used to
     * replicate the remote shared project.
     * @param host the host of the remotly shared project.
     * @param driver the driver of the shared project.
     * @param users the participants of the shared project.
     * @return the shared project.
     */
    public ISharedProject joinSession(IProject project, JID host, 
        JID driver, List<JID> users) {
        
        sharedProject = new SharedProject(transmitter, project, 
            Saros.getDefault().getMyJID(), host, driver, users);
        sharedProject.start();
        
        for (ISessionListener listener : listeners) {
            listener.sessionStarted(sharedProject);
        }
        
        log.info("Session joined");
        
        return sharedProject;
    }
    
    /**
     * Leaves the currently active session. If the local user is the host, this
     * will close the session for everybody.
     * 
     * Has no effect if there is no currently shared project.
     */
    public void leaveSession() {
        if (sharedProject == null)
            return;
        
        transmitter.sendLeaveMessage(sharedProject);
        
        ISharedProject closedProject = sharedProject;
        sharedProject = null;
        
        for (ISessionListener listener : listeners) {
            listener.sessionEnded(closedProject);
        }
        
        log.info("Session left");
    }
    
    /**
     * @return the active SharedProject object or <code>null</code> if there
     * is no active project.
     */
    public ISharedProject getSharedProject() {
        return sharedProject;
    }
    
    /**
     * Add the given session listener. Is ignored if the listener is already
     * listening.
     * 
     * @param listener the listener that is to be added.
     */
    public void addSessionListener(ISessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes the given session listener. Is ignored if the given listener
     * wasn't listening.
     * 
     * @param listener the listener that is to be removed.
     */
    public void removeSessionListener(ISessionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Is fired when an incoming invitation is received.
     * 
     * @param from the sender of this invitation.
     * @param description the informal description text that can be given with
     * invitations.
     * @return the process that represents the invitation and which handles the
     * further interaction with the invitation.
     */
    public IIncomingInvitationProcess invitationReceived(JID from, 
        String description) {
        
        IIncomingInvitationProcess process = new IncomingInvitationProcess(
            transmitter, from, description);
        
        for (ISessionListener listener : listeners) {
            listener.invitationReceived(process);
        }
        
        log.info("Received invitation");
        
        return process;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection, 
        ConnectionState newState) {
        
        if (newState == Saros.ConnectionState.CONNECTED) {
            transmitter = new XMPPChatTransmitter(connection);
            attachRosterListener();
            
        } else {
            leaveSession();
            transmitter = null;
        }
    }
    
    private void attachRosterListener() {
        Roster roster = Saros.getDefault().getRoster();
        roster.addRosterListener(new RosterListener(){
            public void entriesAdded(Collection addresses) {
            }
    
            public void entriesUpdated(Collection addresses) {
            }
    
            public void entriesDeleted(Collection addresses) {
            }
    
            public void presenceChanged(String XMPPAddress) {
                if (sharedProject == null)
                    return;
                
                removeDroppedUserFromSession(XMPPAddress);
            }

            private void removeDroppedUserFromSession(String XMPPAddress) {
                Roster roster = Saros.getDefault().getRoster();
                Presence presence = roster.getPresence(XMPPAddress);
                if (presence == null) {
                    JID jid = new JID(XMPPAddress);
                    User user = sharedProject.getParticipant(jid);
                    
                    if (user != null)
                        sharedProject.removeUser(user);
                }
            }
        });
    }
}
