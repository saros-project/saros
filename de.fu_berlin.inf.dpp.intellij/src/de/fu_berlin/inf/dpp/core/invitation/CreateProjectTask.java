package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import org.apache.log4j.Logger;

import java.io.IOException;

//FIXME: Extract interface /abstract class for core package
//FIXME: Adapt more to IntelliJ (see FIXME below)
public class CreateProjectTask implements IWorkspaceRunnable {
    private static final Logger LOG = Logger.getLogger(CreateProjectTask.class);
    private final String name;
    private final IProject base;
    private final IProgressMonitor monitor;

    private IProject project;

    //TODO: Check if this can be replaced by a static call
    private IWorkspace workspace;

    /**
     * Creates a create project task that can be executed by
     * {@link IWorkspace#run}. The project <b>must not exist</b>.
     *
     * @param name      the name of the new project
     * @param base      project to copy the contents from or <code>null</code> to
     *                  create an empty project
     * @param monitor   monitor that is used for progress report and cancellation or
     *                  <code>null</code> to use the monitor provided by the
     *                  {@link #run(IProgressMonitor)} method
     * @param workspace the workspace that this project resides in
     */
    public CreateProjectTask(String name, IProject base,
        IProgressMonitor monitor, IWorkspace workspace) {

        this.name = name;
        this.base = base;
        this.monitor = monitor;
        this.workspace = workspace;
    }

    /**
     * @return the newly created project or <code>null</code> if it has not been
     * created yet
     */
    public IProject getProject() {
        return project;
    }

    @Override
    public void run(IProgressMonitor monitor) throws IOException {

        if (this.monitor != null) {
            monitor = this.monitor;
        }

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitor.beginTask("Creating project", IProgressMonitor.UNKNOWN);

        project = workspace.getRoot().getProject(name);

        //FIXME: right now creating a directory for project only, later IntelliJ
        // native implementation should be used to create project structure
        if (!project.getFullPath().toFile().mkdirs()) {
            LOG.error("Could not create project: " + project.getName());
            throw new IOException("Could not create project");
        }

        project.refreshLocal();

        monitor.done();
    }
}
