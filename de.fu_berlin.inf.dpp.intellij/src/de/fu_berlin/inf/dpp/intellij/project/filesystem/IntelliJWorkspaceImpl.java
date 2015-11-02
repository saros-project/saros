package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.intellij.project.FileSystemChangeListener;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import org.apache.log4j.Logger;

import java.io.IOException;

public class IntelliJWorkspaceImpl implements IWorkspace {
    public static final Logger LOG = Logger
        .getLogger(IntelliJWorkspaceImpl.class);

    private LocalFileSystem fileSystem;

    private Project project;

    public IntelliJWorkspaceImpl(Project project) {
        this.project = project;
        fileSystem = LocalFileSystem.getInstance();
        fileSystem.addRootToWatch(project.getBasePath(), true);
    }

    @Override
    public void run(IWorkspaceRunnable procedure)
        throws IOException, OperationCanceledException {
        procedure.run(new NullProgressMonitor());
    }

    @Override
    public void run(IWorkspaceRunnable runnable, IResource[] resources)
        throws IOException, OperationCanceledException {
        run(runnable);
    }

    @Override
    public IProject getProject(String projectName) {
        return new IntelliJProjectImpl(project, projectName);
    }

    /**
     * Returns a handle to the project for the given path.
     */
    public IntelliJProjectImpl getProjectForPath(String path) {
        IPath filePath = IntelliJPathImpl.fromString(path);
        IPath projectPath = IntelliJPathImpl.fromString(project.getBasePath());

        if (!projectPath.isPrefixOf(filePath)) {
            return null;
        }

        IPath relativePath = filePath
            .removeFirstSegments(projectPath.segmentCount());

        return new IntelliJProjectImpl(project, relativePath.segment(0));
    }

    @Override
    public IPath getLocation() {
        return IntelliJPathImpl.fromString(project.getBasePath());
    }

    public void addResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.addVirtualFileListener(listener);
    }

    public void removeResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.removeVirtualFileListener(listener);
    }
}
