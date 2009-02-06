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
import java.util.LinkedList;
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
import de.fu_berlin.inf.dpp.net.internal.ConsistencyWatchdogReceiver;
import de.fu_berlin.inf.dpp.net.internal.JupiterReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The SessionManager is responsible for initiating new Saros sessions and for
 * reacting to invitations. The user can be only part of one session at most.
 * 
 * @author rdjemili
 */
public class SessionManager implements IConnectionListener, ISessionManager {
    private static Logger log = Logger
        .getLogger(SessionManager.class.getName());

    private CurrentProjectProxy currentlySharedProject;

    private final List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

    private XMPPChatTransmitter transmitter;

    protected JupiterReceiver jupiterReceiver;

    protected ConsistencyWatchdogReceiver consistencyWatchdogReceiver;

    private String sessionID = NOT_IN_SESSION;

    public ITransmitter getTransmitter() {
        return transmitter;
    }

    public SessionManager(CurrentProjectProxy projectProxy) {
        this.currentlySharedProject = projectProxy;
        Saros.getDefault().addListener(this);
    }

    public void startSession(IProject project) throws XMPPException {
        if (!Saros.getDefault().isConnected()) {
            throw new XMPPException("No connection");
        }

        JID myJID = Saros.getDefault().getMyJID();
        this.sessionID = String.valueOf(new Random(System.currentTimeMillis())
            .nextInt());

        SharedProject sharedProject = new SharedProject(this.transmitter,
            project, myJID);

        this.currentlySharedProject.setVariable(sharedProject);

        sharedProject.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sharedProject);
        }

        sharedProject.startInvitation(null);

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

    public ISharedProject joinSession(IProject project, JID host, int colorID) {

        SharedProject sharedProject = new SharedProject(this.transmitter,
            project, Saros.getDefault().getMyJID(), host, colorID);
        this.currentlySharedProject.setVariable(sharedProject);

        sharedProject.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sharedProject);
        }

        SessionManager.log.info("Session joined");

        return sharedProject;
    }

    public void stopSharedProject() {

        SharedProject project = currentlySharedProject.getVariable();

        if (project == null) {
            return;
        }

        this.transmitter.sendLeaveMessage(project);

        // set resources writable again
        project.setProjectReadonly(false);

        project.stop();

        this.currentlySharedProject.setVariable(null);

        for (ISessionListener listener : this.listeners) {
            listener.sessionEnded(project);
        }

        sessionID = NOT_IN_SESSION;

        SessionManager.log.info("Session left");
    }

    public ISharedProject getSharedProject() {
        return this.currentlySharedProject.getVariable();
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
        String sessionID, String projectName, String description, int colorID) {

        this.sessionID = sessionID;

        IIncomingInvitationProcess process = new IncomingInvitationProcess(
            this.transmitter, from, projectName, description, colorID);

        for (ISessionListener listener : this.listeners) {
            listener.invitationReceived(process);
        }

        SessionManager.log.info("Received invitation");

        return process;
    }

    public interface ConnectionSessionListener {

        public void prepare(XMPPConnection connection);

        public void start();

        public void stop();

        public void dispose();

    }

    List<ConnectionSessionListener> connectionSessionListener = new LinkedList<ConnectionSessionListener>();

    /**
     * Implements the lifecycle management of ConnectionSessionListeners
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {

        switch (newState) {
        case CONNECTED:

            if (connectionSessionListener.isEmpty()) {
                transmitter = new XMPPChatTransmitter();
                connectionSessionListener.add(transmitter);
                connectionSessionListener.add(new JupiterReceiver());
                connectionSessionListener.add(new ConsistencyWatchdogReceiver(
                    transmitter, currentlySharedProject));
                connectionSessionListener.add(new PresenceListener());
            }

            for (ConnectionSessionListener listener : connectionSessionListener) {
                listener.prepare(connection);
            }

            for (ConnectionSessionListener listener : connectionSessionListener) {
                listener.start();
            }

            break;
        case CONNECTING:

            // Cannot do anything until the Connection is up

            break;

        case DISCONNECTING:

            stopSharedProject();
            break;

        case ERROR:

            for (ConnectionSessionListener listener : Util
                .reverse(connectionSessionListener)) {
                listener.stop();
            }
            break;

        case NOT_CONNECTED:

            for (ConnectionSessionListener listener : Util
                .reverse(connectionSessionListener)) {
                listener.stop();
            }

            for (ConnectionSessionListener listener : Util
                .reverse(connectionSessionListener)) {
                listener.dispose();
            }
            connectionSessionListener.clear();
            transmitter = null;
            break;

        }
    }

    public class PresenceListener implements ConnectionSessionListener {

        RosterListener listener = new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
                // ignore
            }

            public void entriesUpdated(Collection<String> addresses) {
                // TODO Check if it affects one of our participants in a session
            }

            public void entriesDeleted(Collection<String> addresses) {
                // TODO Check if it affects one of our participants in a session
            }

            public void presenceChanged(Presence newPresence) {

                String XMPPAddress = newPresence.getFrom();

                SharedProject project = currentlySharedProject.getVariable();

                if (project == null) {
                    return;
                }

                Roster roster = Saros.getDefault().getRoster();

                // TODO Review if this is necessary
                Presence presence = roster.getPresence(XMPPAddress);

                JID jid = new JID(XMPPAddress);
                User user = project.getParticipant(jid);
                if (user != null) {
                    if (presence == null) {
                        user.setPresence(User.UserConnectionState.OFFLINE);

                    } else {
                        user.setPresence(User.UserConnectionState.ONLINE);
                    }
                }

            }
        };

        public void dispose() {
            connection = null;
        }

        public void prepare(XMPPConnection connection) {
            this.connection = connection;
        }

        XMPPConnection connection;

        Roster roster;

        public void registerListener(Roster roster) {

            if (this.roster != null) {
                this.roster.removeRosterListener(listener);
            }
            this.roster = roster;
            if (this.roster != null) {
                this.roster.addRosterListener(listener);
            }
        }

        public void start() {
            if (connection != null) {
                registerListener(connection.getRoster());
            }
        }

        public void stop() {
            registerListener(null);
            connection = null;
        }

    }

    public void OnReconnect(int oldtimestamp) {

        SharedProject project = currentlySharedProject.getVariable();

        if (project == null) {
            return;
        }

        this.transmitter.sendRemainingFiles();
        this.transmitter.sendRemainingMessages();

        // ask for next expected timestamp activities (in case I missed
        // something while being not available)

        // TODO this is currently disabled
        this.transmitter.sendRequestForActivity(project, oldtimestamp, true);
    }
}
