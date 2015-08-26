package de.fu_berlin.inf.dpp.project.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.User.Permission;

/**
 * This model provider is responsible for warning a session participant when
 * trying to modify the file tree of a shared project in an unsupported way.<br>
 * <br>
 * Ignoring the warnings will lead to inconsistencies. Note that we can't
 * prevent these actions, just detect them. Currently detected: File/folder
 * activities as a user with {@link Permission#READONLY_ACCESS}, and deletion of
 * a shared project.
 */
@Component(module = "core")
public class ResourceChangeValidator extends ModelProvider {
    private static final Logger log = Logger
        .getLogger(ResourceChangeValidator.class.getName());

    private static final String ERROR_TEXT = Messages.ResourceChangeValidator_error_no_write_access;
    private static final String DELETE_PROJECT_ERROR_TEXT = Messages.ResourceChangeValidator_error_leave_session_before_delete_project;

    /** Error code for internal use, but we don't need it. */
    private static final int ERROR_CODE = 0;

    private static final IStatus ERROR_STATUS = new Status(IStatus.ERROR,
        "de.fu_berlin.inf.dpp", ERROR_CODE, ERROR_TEXT, null);

    private static final IStatus DELETE_PROJECT_ERROR_STATUS = new Status(
        IStatus.ERROR, "de.fu_berlin.inf.dpp", ERROR_CODE,
        DELETE_PROJECT_ERROR_TEXT, null);

    /** the currently running shared project */
    private ISarosSession sarosSession;

    /**
     * Check each resource delta whether it is in a shared project. If we are
     * not the exclusive user with {@link Permission#WRITE_ACCESS} set the
     * appropriate flag.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        private boolean isAffectingSharedProjectFiles = false;

        private boolean isDeletingSharedProject = false;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.resources.IResourceDeltaVisitor
         */
        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {

            // We already check this in validateChange
            assert sarosSession != null;

            IResource resource = delta.getResource();

            if (resource instanceof IWorkspaceRoot) {
                return true;
            }

            if (resource instanceof IProject) {
                if (!sarosSession.isShared(ResourceAdapterFactory
                    .create(resource)))
                    return false;

                if (delta.getKind() == IResourceDelta.REMOVED) {
                    isDeletingSharedProject = true;
                    return false;
                }

                if (sarosSession.hasExclusiveWriteAccess()) {
                    return false;
                }
                return true;
            }

            isAffectingSharedProjectFiles = true;
            return false;
        }
    }

    @Inject
    protected ISarosSessionManager sessionManager;

    @Override
    protected void initialize() {

        SarosPluginContext.initComponent(this);

        sessionManager.addSessionLifecycleListener(new NullSessionLifecycleListener() {
            @Override
            public void sessionStarted(ISarosSession newSarosSession) {
                sarosSession = newSarosSession;
            }

            @Override
            public void sessionEnded(ISarosSession oldSarosSession) {
                assert sarosSession == oldSarosSession;
                sarosSession = null;
            }
        });
        sarosSession = sessionManager.getSarosSession();
    }

    @Override
    public IStatus validateChange(IResourceDelta delta, IProgressMonitor pm) {

        // If we are currently not sharing a project, we don't have to prevent
        // any file operations
        if (sarosSession == null)
            return Status.OK_STATUS;

        ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();

        try {
            delta.accept(visitor);
        } catch (CoreException e) {
            log.error("Could not run visitor: ", e);
        }

        if (!sarosSession.hasWriteAccess()
            && visitor.isAffectingSharedProjectFiles) {
            return ERROR_STATUS;
        }
        if (visitor.isDeletingSharedProject) {
            return DELETE_PROJECT_ERROR_STATUS;
        }
        return Status.OK_STATUS;
    }
}
