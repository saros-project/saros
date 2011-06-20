package de.fu_berlin.inf.dpp;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;

public class FileListFactory {

    public static FileList createFileList(IProject project,
        List<IResource> resources, boolean useVersionControl,
        SubMonitor subMonitor) throws CoreException {
        if (resources == null)
            return new FileList(project, useVersionControl, subMonitor);

        return new FileList(resources, useVersionControl, subMonitor);
    }

    public static FileList createPathFileList(List<IPath> paths) {
        return new FileList(paths);
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

}
