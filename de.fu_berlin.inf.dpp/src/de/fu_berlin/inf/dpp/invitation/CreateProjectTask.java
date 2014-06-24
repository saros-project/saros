package de.fu_berlin.inf.dpp.invitation;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.Saros;

public class CreateProjectTask implements IWorkspaceRunnable {

    private final String name;
    private final IProject base;
    private final IProgressMonitor monitor;

    private IProject project;

    /**
     * Creates a create project task that can be executed by
     * {@link IWorkspace#run}. The project <b>must not exist</b>.
     * 
     * @param name
     *            the name of the new project
     * @param base
     *            project to copy the contents from or <code>null</code> to
     *            create an empty project
     * @param monitor
     *            monitor that is used for progress report and cancellation or
     *            <code>null</code> to use the monitor provided by the
     *            {@link #run(IProgressMonitor)} method
     */
    public CreateProjectTask(String name, IProject base,
        IProgressMonitor monitor) {

        this.name = name;
        this.base = base;
        this.monitor = monitor;
    }

    /**
     * 
     * @return the newly created project or <code>null</code> if it has not been
     *         created yet
     */
    public IProject getProject() {
        return project;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (this.monitor != null)
            monitor = this.monitor;

        final SubMonitor progress = SubMonitor.convert(monitor,
            "Creating new project... ", base == null ? 2 : 3);

        final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
            .getRoot();

        project = workspaceRoot.getProject(name);

        try {
            if (project.exists())
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                    MessageFormat.format("Project {0} already exists!", name)));

            if (base != null && !base.exists())
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                    MessageFormat.format("Project {0} does not exists!", base)));

            if (project.equals(base))
                throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                    MessageFormat.format(
                        "Project {0} is the same as project {1}!", name, base)));

            project
                .create(progress.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));

            project.open(progress.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));

            progress.subTask("refreshing file contents");
            project.refreshLocal(IResource.DEPTH_INFINITE,
                progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));

            progress.subTask("clearing history");
            project.clearHistory(progress.newChild(1,
                SubMonitor.SUPPRESS_ALL_LABELS));

            if (base != null) {
                progress.subTask("copying contents from project "
                    + base.getName());
                base.copy(project.getFullPath(), true,
                    progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
            }
        } finally {
            if (monitor != null)
                monitor.done();
        }
    }
}
