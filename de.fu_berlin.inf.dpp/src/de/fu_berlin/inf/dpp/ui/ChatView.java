package de.fu_berlin.inf.dpp.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class ChatView extends ViewPart implements ISessionListener, IConnectionListener {

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionEnded(ISharedProject session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionStarted(ISharedProject session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionStateChanged(XMPPConnection connection,
			ConnectionState newState) {
		// TODO Auto-generated method stub
		
	}

}


