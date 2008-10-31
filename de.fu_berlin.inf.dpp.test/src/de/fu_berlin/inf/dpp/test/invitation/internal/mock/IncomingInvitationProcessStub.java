package de.fu_berlin.inf.dpp.test.invitation.internal.mock;

import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;

public class IncomingInvitationProcessStub implements
	IIncomingInvitationProcess {
    private int seconds;

    public IncomingInvitationProcessStub() {
    }

    public IncomingInvitationProcessStub(int seconds) {
	this.seconds = seconds;
    }

    public FileList requestRemoteFileList(IProgressMonitor monitor) {
	if (seconds > 0) {
	    waitWithProgressMonitor(monitor);
	}

	return new FileList();
    }

    public void accept(IProject localProject, String newProjectName,
	    IProgressMonitor monitor, boolean copy) {

	if (seconds > 0) {
	    waitWithProgressMonitor(monitor);
	}
    }

    public FileList getRemoteFileList() {
	return new FileList();
    }

    public Exception getException() {
	return null;
    }

    public State getState() {
	return null;
    }

    public JID getPeer() {
	return new JID("jid@test.org");
    }

    public String getDescription() {
	return "test description";
    }

    public void fileListReceived(JID from, FileList fileList) {
    }

    public void fileListRequested(JID from) {
    }

    public void joinReceived(JID from) {
    }

    public void resourceSent(IPath path) {
    }

    public void resourceReceived(JID from, IPath path, InputStream input) {
    }

    public void cancel(String errorMsg, boolean replicated) {
    }

    private void waitWithProgressMonitor(IProgressMonitor monitor) {
	monitor.beginTask("test wait", IProgressMonitor.UNKNOWN);

	long start = System.currentTimeMillis();
	while (System.currentTimeMillis() < start + seconds * 1000) {
	    try {
		Thread.sleep(200);
		monitor.worked(1);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	monitor.done();
    }

    public void setInvitationUI(IInvitationUI inviteUI) {
	// TODO Auto-generated method stub

    }

    public String getProjectName() {
	// TODO Auto-generated method stub
	return null;
    }

    public void invitationAccepted(JID from) {
	// TODO Auto-generated method stub

    }

    public TransferMode getTransferMode() {
	// TODO Auto-generated method stub
	return null;
    }

    public void jingleFallback() {
	// TODO Auto-generated method stub

    }

    public void fileSent(IPath path) {
	// TODO Auto-generated method stub

    }

    public void fileTransferFailed(IPath path, Exception e) {
	// TODO Auto-generated method stub

    }

    public void transferProgress(int transfered) {
	// TODO Auto-generated method stub

    }

    public void setTransferMode(TransferMode mode) {
	// TODO Auto-generated method stub

    }

    @Override
    public void accept(IProject baseProject, String newProjectName,
	    IProgressMonitor monitor) {
	// TODO Auto-generated method stub

    }
}
