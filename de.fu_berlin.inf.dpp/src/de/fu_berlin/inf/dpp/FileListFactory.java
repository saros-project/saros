package de.fu_berlin.inf.dpp;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.project.IChecksumCache;

public class FileListFactory {

    public static FileList createFileList(IProject project,
        List<IResource> resources, IChecksumCache checksumCache,
        boolean useVersionControl, IProgressMonitor monitor)
        throws CoreException {

        if (resources == null)
            return new FileList(project, checksumCache, useVersionControl,
                monitor);

        return new FileList(resources, checksumCache, useVersionControl,
            monitor);
    }

    public static FileList createPathFileList(List<IPath> paths) {
        return new FileList(paths);
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

}
