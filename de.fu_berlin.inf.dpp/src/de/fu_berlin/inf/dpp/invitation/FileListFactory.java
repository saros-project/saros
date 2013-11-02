package de.fu_berlin.inf.dpp.invitation;

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

    /**
     * Creates a new file list from given paths. It does not compute checksums
     * or location information.
     * 
     * @NOTE This method does not check the input. The caller is
     *       <b>responsible</b> for the <b>correct</b> input !
     * 
     * @param paths
     *            a list of paths that <b>refers</b> to <b>files</b> that should
     *            be added to this file list.
     */
    public static FileList createPathFileList(List<IPath> paths) {
        return new FileList(paths);
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

}
