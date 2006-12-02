package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;

public class SharedDocumentProvider extends TextFileDocumentProvider implements ISessionListener,
	ISharedProjectListener {

	private ISharedProject sharedProject;

	private boolean isDriver;

	public SharedDocumentProvider() {
		SessionManager sm = Saros.getDefault().getSessionManager();
		if (sm.getSharedProject() != null)
			sessionStarted(sm.getSharedProject());

		sm.addSessionListener(this);
	}

	@Override
	public boolean isReadOnly(Object element) {
		if (sharedProject == null || !isInSharedProject(element))
			return super.isReadOnly(element);

		return !isDriver || super.isReadOnly(element);
	}

	@Override
	public boolean isModifiable(Object element) {
		if (sharedProject == null || !isInSharedProject(element))
			return super.isModifiable(element);

		return isDriver && super.isModifiable(element);
	}

	@Override
	public boolean canSaveDocument(Object element) {
		if (sharedProject == null || !isInSharedProject(element))
			return super.canSaveDocument(element);

		return isDriver && super.canSaveDocument(element);
	}

	@Override
	public boolean mustSaveDocument(Object element) {
		if (sharedProject == null || !isInSharedProject(element))
			return super.mustSaveDocument(element);

		return isDriver && super.mustSaveDocument(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess process) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		sharedProject = session;
		isDriver = sharedProject.isDriver();

		sharedProject.addListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		sharedProject = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void driverChanged(JID driver, boolean replicated) {
		isDriver = sharedProject.isDriver(); // HACK
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userJoined(JID user) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
	 */
	public void userLeft(JID user) {
		// ignore
	}

	private boolean isInSharedProject(Object element) {
		IFileEditorInput fileEditorInput = (IFileEditorInput) element;
		IProject project = fileEditorInput.getFile().getProject();

		return project.equals(sharedProject.getProject());
	}
}