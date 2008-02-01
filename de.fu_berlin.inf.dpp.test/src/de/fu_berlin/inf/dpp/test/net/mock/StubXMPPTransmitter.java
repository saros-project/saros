package de.fu_berlin.inf.dpp.test.net.mock;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class StubXMPPTransmitter implements ITransmitter, FileTransferListener{

	@Override
	public void addInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendActivities(ISharedProject sharedProject,
			List<TimedActivity> activities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendCancelInvitationMessage(JID jid, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFile(JID recipient, IPath path,
			IFileTransferCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFile(JID recipient, IPath path, int timestamp,
			IFileTransferCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFileList(JID jid, FileList fileList) throws XMPPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendInviteMessage(ISharedProject sharedProject, JID jid,
			String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendJoinMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendLeaveMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRemainingFiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRemainingMessages() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRequestForActivity(ISharedProject sharedProject,
			int timestamp, boolean andup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRequestForFileListMessage(JID recipient) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendUserListTo(JID to, List<User> participants) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXMPPConnection(XMPPConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		// TODO Auto-generated method stub
		
	}

}
