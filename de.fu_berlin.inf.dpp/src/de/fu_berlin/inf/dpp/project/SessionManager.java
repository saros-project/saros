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
	private static Logger log = Logger.getLogger(SessionManager.class.getName());

	private SharedProject sharedProject;

	// TODO use ListenerList instead
	private List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

	ITransmitter transmitter;

	public SessionManager() {
		Saros.getDefault().addListener(this);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#startSession(org.eclipse.core.resources.IProject)
	 */
	public void startSession(IProject project) throws XMPPException {
		if (!Saros.getDefault().isConnected()) {
			throw new XMPPException("No connection");
		}

		JID myJID = Saros.getDefault().getMyJID();
		sharedProject = new SharedProject(transmitter, project, myJID);
		sharedProject.start();
		
		for (ISessionListener listener : listeners) {
			listener.sessionStarted(sharedProject);
		}

		sharedProject.startInvitation(null);
		
		log.info("Session started");
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#joinSession(org.eclipse.core.resources.IProject, de.fu_berlin.inf.dpp.net.JID, de.fu_berlin.inf.dpp.net.JID, java.util.List)
	 */
	public ISharedProject joinSession(IProject project, JID host, JID driver, List<JID> users) {

		sharedProject = new SharedProject(transmitter, project, Saros.getDefault().getMyJID(),
			host, driver, users);
		sharedProject.start();

		for (ISessionListener listener : listeners) {
			listener.sessionStarted(sharedProject);
		}

		log.info("Session joined");

		return sharedProject;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#leaveSession()
	 */
	public void leaveSession() {
		if (sharedProject == null)
			return;

		transmitter.sendLeaveMessage(sharedProject);
		sharedProject.setProjectReadonly(false);	// set ressources writeable again

		sharedProject.stop();

		ISharedProject closedProject = sharedProject;
		sharedProject = null;

		for (ISessionListener listener : listeners) {
			listener.sessionEnded(closedProject);
		}

		log.info("Session left");
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#getSharedProject()
	 */
	public ISharedProject getSharedProject() {
		return sharedProject;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#addSessionListener(de.fu_berlin.inf.dpp.project.ISessionListener)
	 */
	public void addSessionListener(ISessionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#removeSessionListener(de.fu_berlin.inf.dpp.project.ISessionListener)
	 */
	public void removeSessionListener(ISessionListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#invitationReceived(de.fu_berlin.inf.dpp.net.JID, java.lang.String, java.lang.String)
	 */
	public IIncomingInvitationProcess invitationReceived(JID from, String projectName,
		String description) {

		IIncomingInvitationProcess process = new IncomingInvitationProcess(transmitter, from,
			projectName, description);

		for (ISessionListener listener : listeners) {
			listener.invitationReceived(process);
		}

		log.info("Received invitation");

		return process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
	 */
	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#connectionStateChanged(org.jivesoftware.smack.XMPPConnection, de.fu_berlin.inf.dpp.Saros.ConnectionState)
	 */
	public void connectionStateChanged(XMPPConnection connection, ConnectionState newState) {

		if (newState == Saros.ConnectionState.CONNECTED) {
			if (transmitter==null) {
				transmitter = new XMPPChatTransmitter(connection);
				attachRosterListener();
			}
			else
				transmitter.setXMPPConnection(connection);
			
			

		} else if (newState == Saros.ConnectionState.NOT_CONNECTED) { 
			if (sharedProject != null) 
				leaveSession();
			
			transmitter = null;
		}
	}

	private void attachRosterListener() {
		Roster roster = Saros.getDefault().getRoster();
		roster.addRosterListener(new RosterListener() {
			public void entriesAdded(Collection addresses) {
			}

			public void entriesUpdated(Collection<String> addresses) {
			}

			public void entriesDeleted(Collection<String> addresses) {
			}

			public void presenceChanged(String XMPPAddress) {
				
				if (sharedProject==null)
					return;
				
				Roster roster = Saros.getDefault().getRoster();
				Presence presence = roster.getPresence(XMPPAddress);

				JID jid = new JID(XMPPAddress);
				User user = sharedProject.getParticipant(jid);
				if (user!=null){
					if (presence==null) {
						user.setPresence( User.UserConnectionState.OFFLINE);
						
					} else
						user.setPresence( User.UserConnectionState.ONLINE );
				}
			}

			
			public void presenceChanged(Presence presence) {
				//TODO: new Method for Smack 3
				presenceChanged(presence.getFrom());
				
			}

		});
	}
	
	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISessionManager#OnReconnect(int)
	 */
	public void OnReconnect(int oldtimestamp){

		if (sharedProject==null)
			return;
		
		transmitter.sendRemainingFiles();
		transmitter.sendRemainingMessages();

		// ask for next expected timestamp activities (in case I missed something while being not available)
		transmitter.sendRequestForActivity( sharedProject, oldtimestamp, true );
	}
}
