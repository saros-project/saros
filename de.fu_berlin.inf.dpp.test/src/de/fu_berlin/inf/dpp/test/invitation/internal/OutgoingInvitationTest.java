package de.fu_berlin.inf.dpp.test.invitation.internal;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.test.XMPPTestCase;
import de.fu_berlin.inf.dpp.test.invitation.internal.mock.MockOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.test.util.FileListHelper;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

public class OutgoingInvitationTest extends TestCase{


	
	public void testSendMissingfile() throws CoreException, XMPPException {
		IProject project = ResourceHelper.getProject("SmalProject");
		FileList list = FileListHelper.createFielListForProject("SmalProject");
		System.out.println(list.toXML());
		
		
		
	}
}
