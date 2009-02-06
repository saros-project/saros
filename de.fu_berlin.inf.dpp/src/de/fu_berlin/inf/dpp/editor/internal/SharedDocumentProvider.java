package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

public class SharedDocumentProvider extends TextFileDocumentProvider implements
    ISessionListener, ISharedProjectListener {

    private ISharedProject sharedProject;

    private boolean isDriver;

    public SharedDocumentProvider() {
        ISessionManager sm = Saros.getDefault().getSessionManager();
        if (sm.getSharedProject() != null) {
            sessionStarted(sm.getSharedProject());
        }

        sm.addSessionListener(this);
    }

    @Override
    public boolean isReadOnly(Object element) {
        if ((this.sharedProject == null) || !isInSharedProject(element)) {
            return super.isReadOnly(element);
        }

        return !this.isDriver || super.isReadOnly(element);
    }

    @Override
    public boolean isModifiable(Object element) {
        if ((this.sharedProject == null) || !isInSharedProject(element)) {
            return super.isModifiable(element);
        }

        return this.isDriver && super.isModifiable(element);
    }

    @Override
    public boolean canSaveDocument(Object element) {
        if ((this.sharedProject == null) || !isInSharedProject(element)) {
            return super.canSaveDocument(element);
        }

        return this.isDriver && super.canSaveDocument(element);
    }

    @Override
    public boolean mustSaveDocument(Object element) {
        if ((this.sharedProject == null) || !isInSharedProject(element)) {
            return super.mustSaveDocument(element);
        }

        return this.isDriver && super.mustSaveDocument(element);
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
        this.sharedProject = session;
        this.isDriver = this.sharedProject.isDriver();

        this.sharedProject.addListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        this.sharedProject = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
        if (this.sharedProject != null) {
            this.isDriver = this.sharedProject.isDriver(); // HACK
        }
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

        return project.equals(this.sharedProject.getProject());
    }
}