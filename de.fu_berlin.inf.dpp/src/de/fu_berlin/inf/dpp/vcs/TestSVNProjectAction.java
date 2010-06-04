package de.fu_berlin.inf.dpp.vcs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.SessionManager;

public class TestSVNProjectAction implements IObjectActionDelegate {
    protected IProject selectedProject;
    protected IWorkbenchPart targetPart;
    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected Saros saros;

    public TestSVNProjectAction() {
        super();
        Saros.reinject(this);
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    protected IProject getProject(ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof IResource) {
            return ((IResource) element).getProject();
        }
        return null;
    }

    public void run(IAction action) {
        IProject project = selectedProject;

        if (RepositoryProvider.isShared(project)) {
            RepositoryProvider provider = RepositoryProvider
                .getProvider(project);
            // log.info("Project " + project + " is shared using "
            // + provider.getID());
            if (provider.getID().equals(
                "org.tigris.subversion.subclipse.core.svnnature")) {
                IPath path = saros.getStateLocation();
                SVNTest.testApplyPatch(project, targetPart, saros);
            }
        }

        // SVNTest.doStuff(selectedProject);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selectedProject = getProject(selection);

        action.setEnabled(saros.isConnected()
            // && sessionManager.getSharedProject() != null
            && (this.selectedProject != null)
            && this.selectedProject.isAccessible());
        // && !sessionManager.getSharedProject()
        // .isShared(this.selectedProject));

    }
}
