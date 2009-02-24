package de.fu_berlin.inf.dpp.editor.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

public class SharedDocumentProvider extends TextFileDocumentProvider implements
    ISessionListener {

    private ISharedProject sharedProject;

    private boolean isDriver;

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user, boolean replicated) {
            if (SharedDocumentProvider.this.sharedProject != null) {
                SharedDocumentProvider.this.isDriver = SharedDocumentProvider.this.sharedProject
                    .isDriver(); // HACK
            }
        }
    };

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

    public void invitationReceived(IIncomingInvitationProcess process) {
        // We are started later during the sessionStarted Method, not before.
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        this.sharedProject = session;
        this.isDriver = this.sharedProject.isDriver();

        this.sharedProject.addListener(this.sharedProjectListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        this.sharedProject.removeListener(this.sharedProjectListener);
        this.sharedProject = null;
    }

    private boolean isInSharedProject(Object element) {
        IFileEditorInput fileEditorInput = (IFileEditorInput) element;
        IProject project = fileEditorInput.getFile().getProject();

        return project.equals(this.sharedProject.getProject());
    }
}