/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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
import java.util.Random;
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
public class SessionManager implements IConnectionListener, ISessionManager {
    private static Logger log = Logger
            .getLogger(SessionManager.class.getName());

    private SharedProject sharedProject;

    // TODO use ListenerList instead
    private final List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

    private ITransmitter transmitter;

    private String sessionID;

    public ITransmitter getTransmitter() {
        return transmitter;
    }

    public SessionManager() {
        Saros.getDefault().addListener(this);
    }

    public void startSession(IProject project) throws XMPPException {
        if (!Saros.getDefault().isConnected()) {
            throw new XMPPException("No connection");
        }

        JID myJID = Saros.getDefault().getMyJID();
        this.sessionID = String.valueOf(new Random(System.currentTimeMillis())
                .nextInt());
        this.sharedProject = new SharedProject(this.transmitter, project, myJID);
        this.sharedProject.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(this.sharedProject);
        }

        this.sharedProject.startInvitation(null);

        SessionManager.log.info("Session started");
    }

    /**
     * Every Session is identified by an int as identifier.
     * 
     * @return the session id of this session
     */
    public String getSessionID() {
        return sessionID;
    }

    public ISharedProject joinSession(IProject project, JID host, JID driver,
            List<JID> users, int colorID) {

        this.sharedProject = new SharedProject(this.transmitter, project, Saros
                .getDefault().getMyJID(), host, driver, users, colorID);
        this.sharedProject.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(this.sharedProject);
        }

        SessionManager.log.info("Session joined");

        return this.sharedProject;
    }

    public void leaveSession() {
        if (this.sharedProject == null) {
            return;
        }

        this.transmitter.sendLeaveMessage(this.sharedProject);
        this.sharedProject.setProjectReadonly(false); // set ressources
        // writeable again

        this.sharedProject.stop();

        ISharedProject closedProject = this.sharedProject;
        this.sharedProject = null;

        for (ISessionListener listener : this.listeners) {
            listener.sessionEnded(closedProject);
        }

        SessionManager.log.info("Session left");
    }

    public ISharedProject getSharedProject() {
        return this.sharedProject;
    }

    public void addSessionListener(ISessionListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeSessionListener(ISessionListener listener) {
        this.listeners.remove(listener);
    }

    public IIncomingInvitationProcess invitationReceived(JID from,
            String sessionID, String projectName, String description,
            int colorID) {

        this.sessionID = sessionID;

        IIncomingInvitationProcess process = new IncomingInvitationProcess(
                this.transmitter, from, projectName, description, colorID);

        for (ISessionListener listener : this.listeners) {
            listener.invitationReceived(process);
        }

        SessionManager.log.info("Received invitation");

        return process;
    }

    public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {

        if (newState == Saros.ConnectionState.CONNECTED) {
            if (this.transmitter == null) {
                this.transmitter = new XMPPChatTransmitter(connection);
                attachRosterListener();
            } else {
                // TODO: Does this ever happen?
                this.transmitter.setXMPPConnection(connection);
            }

        } else if (newState == Saros.ConnectionState.NOT_CONNECTED) {
            if (this.sharedProject != null) {
                leaveSession();
            }

            this.transmitter = null;
        }
    }

    private void attachRosterListener() {
        Roster roster = Saros.getDefault().getRoster();
        roster.addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
            }

            public void entriesUpdated(Collection<String> addresses) {
            }

            public void entriesDeleted(Collection<String> addresses) {
            }

            public void presenceChanged(String XMPPAddress) {

                if (SessionManager.this.sharedProject == null) {
                    return;
                }

                Roster roster = Saros.getDefault().getRoster();
                Presence presence = roster.getPresence(XMPPAddress);

                JID jid = new JID(XMPPAddress);
                User user = SessionManager.this.sharedProject
                        .getParticipant(jid);
                if (user != null) {
                    if (presence == null) {
                        user.setPresence(User.UserConnectionState.OFFLINE);

                    } else {
                        user.setPresence(User.UserConnectionState.ONLINE);
                    }
                }
            }

            public void presenceChanged(Presence presence) {
                // TODO: new Method for Smack 3
                presenceChanged(presence.getFrom());

            }

        });
    }

    public void OnReconnect(int oldtimestamp) {

        if (this.sharedProject == null) {
            return;
        }

        this.transmitter.sendRemainingFiles();
        this.transmitter.sendRemainingMessages();

        // ask for next expected timestamp activities (in case I missed
        // something while being not available)
        this.transmitter.sendRequestForActivity(this.sharedProject,
                oldtimestamp, true);
    }
}
