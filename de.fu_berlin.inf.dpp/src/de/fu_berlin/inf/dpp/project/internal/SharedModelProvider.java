package de.fu_berlin.inf.dpp.project.internal;

import org.apache.log4j.Logger;
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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This model provider is responsible for preventing an session observer from
 * modifying the file tree of a shared project on his own.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class SharedModelProvider extends ModelProvider {

    private static final Logger log = Logger
        .getLogger(SharedModelProvider.class.getName());

    private static final String ERROR_TEXT = "Only the driver should edit the resources of this shared project.";
    private static final String EXCLUSIVE_ERROR_TEXT = "The project host should be the exclusive driver to edit resources of this shared project.";

    private static final IStatus ERROR_STATUS = new Status(IStatus.ERROR,
        "de.fu_berlin.inf.dpp", 2, SharedModelProvider.ERROR_TEXT, null);

    private static final IStatus EXCLUSIVE_ERROR_STATUS = new Status(
        IStatus.ERROR, "de.fu_berlin.inf.dpp", 2,
        SharedModelProvider.EXCLUSIVE_ERROR_TEXT, null);

    /** the currently running shared project */
    private ISharedProject sharedProject;

    /**
     * Check each resource delta whether it is in a shared project. If we are
     * not the exclusive driver set the appropriate flag.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        private boolean isAffectingSharedProjectFiles = false;

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

            if (!sharedProject.isShared(resource.getProject()))
                return false;

            if ((resource instanceof IFile) || (resource instanceof IFolder)) {
                this.isAffectingSharedProjectFiles = true;
                return false;
            }

            return true;
        }
    }

    @Inject
    protected SessionManager sessionManager;

    @Override
    protected void initialize() {

        Saros.reinject(this);

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject project) {
                sharedProject = project;
            }

            @Override
            public void sessionEnded(ISharedProject project) {
                assert sharedProject == project;
                sharedProject = null;
            }
        });
        this.sharedProject = sessionManager.getSharedProject();
    }

    @Override
    public IStatus validateChange(IResourceDelta delta, IProgressMonitor pm) {

        // If we are currently not sharing a project, we don't have to prevent
        // any file operations
        if (this.sharedProject == null)
            return Status.OK_STATUS;

        if (this.sharedProject.isExclusiveDriver())
            return Status.OK_STATUS;

        ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();

        try {
            delta.accept(visitor);
        } catch (CoreException e) {
            log.error("Could not run visitor: ", e);
        }

        if (!sharedProject.isDriver() && visitor.isAffectingSharedProjectFiles) {
            return SharedModelProvider.ERROR_STATUS;
        }
        if (!sharedProject.isExclusiveDriver()
            && visitor.isAffectingSharedProjectFiles) {
            return SharedModelProvider.EXCLUSIVE_ERROR_STATUS;
        }
        return Status.OK_STATUS;
    }
}
