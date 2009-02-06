package de.fu_berlin.inf.dpp.project.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * This model provider is responsible for preventing an session observer from
 * modifying the file tree of a shared project on his own.
 * 
 * @author rdjemili
 */
public class SharedModelProvider extends ModelProvider implements
    ISessionListener {

    private static final String ERROR_TEXT = "Only the driver should edit the resources of this shared project.";
    private static final String EXCLUSIVE_ERROR_TEXT = "The project host should be the exclusive driver to edit resources of this shared project.";

    private static final IStatus ERROR_STATUS = new Status(IStatus.ERROR,
        "de.fu_berlin.inf.dpp", 2, SharedModelProvider.ERROR_TEXT, null);

    private static final IStatus EXCLUSIVE_ERROR_STATUS = new Status(
        IStatus.ERROR, "de.fu_berlin.inf.dpp", 2,
        SharedModelProvider.EXCLUSIVE_ERROR_TEXT, null);

    /** the currently running shared project */
    private ISharedProject sharedProject;

    public SharedModelProvider() {

    }

    /**
     * Check each resource delta whether it is in a shared project. If we are
     * not the exclusive driver set the appropriate flag.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {
        private boolean isAllowed = true;
        private boolean isExclusive = true;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.resources.IResourceDeltaVisitor
         */
        public boolean visit(IResourceDelta delta) throws CoreException {

            // We already check this in validateChange
            assert SharedModelProvider.this.sharedProject != null;

            IResource resource = delta.getResource();

            // If workspace root continue
            if (resource.getProject() == null) {
                return true;
            }

            if (resource.getProject() != SharedModelProvider.this.sharedProject
                .getProject()) {
                return false;
            }

            if (SharedModelProvider.this.sharedProject.isDriver()) {

                // TODO Hard-coded Host
                /* check driver status */
                if (!SharedModelProvider.this.sharedProject.isHost()
                    || !SharedModelProvider.this.sharedProject
                        .isExclusiveDriver()) {
                    this.isExclusive = false;
                }
                return false;
            }

            if ((resource instanceof IFile) || (resource instanceof IFolder)) {
                this.isAllowed = false;
                return false;
            }

            return delta.getKind() != IResourceDelta.NO_CHANGE;
        }
    }

    @Override
    protected void initialize() {
        ISessionManager sm = Saros.getDefault().getSessionManager();

        sm.addSessionListener(this);
        this.sharedProject = sm.getSharedProject();
    }

    @Override
    public IStatus validateChange(IResourceDelta delta, IProgressMonitor pm) {

        // If we are currently not sharing a project, we don't have to prevent
        // any file operations
        if (this.sharedProject == null)
            return Status.OK_STATUS;

        ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();

        try {
            delta.accept(visitor);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        IStatus result = Status.OK_STATUS;
        if (!visitor.isAllowed) {
            result = SharedModelProvider.ERROR_STATUS;
        }
        if (!visitor.isExclusive) {
            result = SharedModelProvider.EXCLUSIVE_ERROR_STATUS;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        this.sharedProject = session;
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
     * @see de.fu_berlin.inf.dpp.project.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // ignore
    }
}
