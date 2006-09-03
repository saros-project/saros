package de.fu_berlin.inf.dpp.ecg;

import org.eclipse.core.runtime.IPath;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.MessagingManager.IChatListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

/**
 * This sensor listens for all distributed pair programming related events. The
 * supported events are:
 * 
 * <ul>
 * <li>Starting/ending dpp sessions</li>
 * <li>Inviting/receiving invitations to dpp session</li>
 * <li>Give driver role</li>
 * <li>Activate/deactivate follow mode</li>
 * <li>Sending/reciving instant messages</li>
 * <li>User arrivales/departments</li>
 * </ul>
 * 
 * @author rdjemili
 */
public class SarosSensor implements ISessionListener, ISharedProjectListener, 
    IChatListener, ISharedEditorListener {
    
	private static final String MSDT_DPP_SESSION     = "msdt.dppsession.xsd";
    private static final String MSDT_DPP_PARTICIPANT = "msdt.dppparticipant.xsd";
    private static final String MSDT_DPP_CHAT        = "msdt.dppchat.xsd";
    private static final String MSDT_EDITOR          = "msdt.dppeditor.xsd";

	private ISharedProject sharedProject;
	
    public SarosSensor() {
        Saros saros = Saros.getDefault();
        saros.getSessionManager().addSessionListener(this);
        saros.getMessagingManager().addChatListener(this);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
    	sharedProject = session;
        session.addListener(this);
        EditorManager.getDefault().addSharedEditorListener(this);
        
        processSession("started");
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        processSession("ended");
        
        sharedProject = null;
        session.removeListener(this);
        EditorManager.getDefault().removeSharedEditorListener(this);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess invitation) {
    	processSession("invited");
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
    	processParticipant("driverChanged", driver.toString());
    }

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userJoined(JID user) {
		processParticipant("joined", user.toString());
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userLeft(JID user) {
		processParticipant("left", user.toString());
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.MessagingManager.IChatListener
	 */
	public void chatMessageAdded(String sender, String message) {
	    processChat(sender, message);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
	 */
	public void followModeChanged(boolean enabled) {
		String text = enabled ? "followActivated" : "followDeactivated";
		processEditor(text, null);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
	 */
	public void activeDriverEditorChanged(IPath path, boolean replicated) {
		if (replicated)
			processEditor("activated", path);
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.editor.ISharedEditorListener
	 */
	public void driverEditorRemoved(IPath path, boolean replicated) {
		if (replicated)
			processEditor("closed", path);
	}

	public void driverEditorSaved(IPath path, boolean replicated) {
		// ignore
	}
	
	private void processEditor(String activity, IPath path) {
    	String xml = "<dppEditor><activity>"+activity+"</activity>" +
			"<path>"+path+"</path></dppEditor>"; 
    	process(MSDT_EDITOR, xml);
    }

	private void processSession(String activity) {
    	String xml = "<dppSession><activity>"+activity+"</activity></dppSession>"; 
    	process(MSDT_DPP_SESSION, xml);
    }
    
    private void processParticipant(String activity, String user) {
    	String xml = "<dppParticipant><activity>"+activity+"</activity>" +
    		"<user>"+user+"</user></dppParticipant>";
    	process(MSDT_DPP_PARTICIPANT, xml);
    }
    
    private void processChat(String sender, String message) {
    	String xml = "<dppChat><user>"+sender+"</user>" +
        	"<message>"+message+"</message></dppChat>";
        process(MSDT_DPP_CHAT, xml);
    }
    
    private void process(String namespace, String xml) {
		ECGEclipseSensor.getInstance().processActivity(namespace, wrap(xml));
	}

	private String wrap(String text) {
    	String username = System.getenv("username");
    	
    	String project = (sharedProject != null) ? 
			sharedProject.getProject().getName() : null;
    	
    	StringBuffer full = new StringBuffer();
    	full.append("<?xml version=\"1.0\"?><microActivity>");
    	full.append("<commonData><username>");
    	full.append(username);
		full.append("</username><projectname>");
		full.append(project);
		full.append("</projectname></commonData>");
		
		full.append(text);
		
		full.append("</microActivity>");
		return full.toString();
    }
}
