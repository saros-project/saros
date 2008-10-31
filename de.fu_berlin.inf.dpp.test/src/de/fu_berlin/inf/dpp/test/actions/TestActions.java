package de.fu_berlin.inf.dpp.test.actions;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;

public class TestActions extends TestCase {

    public void testJoinWizard() throws Exception {
	// SarosUI s = new SarosUI(new SessionManagerStub());
	IncomingInvitationProcessStub iistub = new IncomingInvitationProcessStub();
	// iistub.setTransferMode(TransferMode.JINGLE);
	// s.invitationReceived(iistub);

	Shell shell = Display.getDefault().getActiveShell();
	new WizardDialog(shell, new JoinSessionWizard(iistub)).open();
	// while(true){
	// Thread.sleep(300);
	// }
    }

    private class IncomingInvitationProcessStub implements
	    IIncomingInvitationProcess {

	private TransferMode mode = TransferMode.IBB;

	public void accept(IProject baseProject, String newProjectName,
		IProgressMonitor monitor, boolean copy) {
	    // TODO Auto-generated method stub

	}

	public FileList getRemoteFileList() {
	    FileList list = null;
	    try {
		IProject project = ResourceHelper.createProject("Transfer");
		list = new FileList(project);
	    } catch (CoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return list;
	}

	public FileList requestRemoteFileList(IProgressMonitor monitor) {
	    waitWithProgressMonitor(monitor);
	    return null;
	}

	public void setInvitationUI(IInvitationUI inviteUI) {
	    // TODO Auto-generated method stub

	}

	public void cancel(String errorMsg, boolean replicated) {
	    // TODO Auto-generated method stub

	}

	public void fileListReceived(JID from, FileList fileList) {
	    // TODO Auto-generated method stub

	}

	public Exception getException() {
	    // TODO Auto-generated method stub
	    return null;
	}

	public JID getPeer() {
	    return new JID("jid@test.org");
	}

	public String getDescription() {
	    return "test description";
	}

	public String getProjectName() {

	    return "Transfer";
	}

	public State getState() {
	    // TODO Auto-generated method stub
	    return null;
	}

	public TransferMode getTransferMode() {
	    return this.mode;
	}

	public void invitationAccepted(JID from) {
	    // TODO Auto-generated method stub

	}

	public void joinReceived(JID from) {
	    // TODO Auto-generated method stub

	}

	public void resourceReceived(JID from, IPath path, InputStream input) {
	    // TODO Auto-generated method stub

	}

	public void fileSent(IPath path) {
	    // TODO Auto-generated method stub

	}

	public void fileTransferFailed(IPath path, Exception e) {
	    // TODO Auto-generated method stub

	}

	public void jingleFallback() {
	    // TODO Auto-generated method stub

	}

	public void setTransferMode(TransferMode mode) {
	    this.mode = mode;
	}

	public void transferProgress(int transfered) {
	    // TODO Auto-generated method stub

	}

	private void waitWithProgressMonitor(IProgressMonitor monitor) {
	    monitor.beginTask("test wait", IProgressMonitor.UNKNOWN);

	    long start = System.currentTimeMillis();
	    while (System.currentTimeMillis() < start + 1 * 1000) {
		try {
		    Thread.sleep(200);
		    monitor.worked(1);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	    monitor.done();
	}

	@Override
	public void accept(IProject baseProject, String newProjectName,
		IProgressMonitor monitor) {
	    // TODO Auto-generated method stub

	}
    }

    private class SessionManagerStub implements ISessionManager {

	public void OnReconnect(int oldtimestamp) {
	    // TODO Auto-generated method stub

	}

	public void addSessionListener(ISessionListener listener) {
	    // TODO Auto-generated method stub

	}

	public void connectionStateChanged(XMPPConnection connection,
		ConnectionState newState) {
	    // TODO Auto-generated method stub

	}

	public ISharedProject getSharedProject() {
	    // TODO Auto-generated method stub
	    return null;
	}

	public IIncomingInvitationProcess invitationReceived(JID from,
		String projectName, String description) {
	    // TODO Auto-generated method stub
	    return null;
	}

	public ISharedProject joinSession(IProject project, JID host,
		JID driver, List<JID> users) {
	    // TODO Auto-generated method stub
	    return null;
	}

	public void leaveSession() {
	    // TODO Auto-generated method stub

	}

	public void removeSessionListener(ISessionListener listener) {
	    // TODO Auto-generated method stub

	}

	public void startSession(IProject project) throws XMPPException {
	    // TODO Auto-generated method stub

	}

    }
}
