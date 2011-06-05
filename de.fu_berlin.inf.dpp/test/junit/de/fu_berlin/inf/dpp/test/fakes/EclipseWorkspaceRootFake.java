package de.fu_berlin.inf.dpp.test.fakes;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;

import java.io.File;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IWorkspaceRoot}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionallity which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
public class EclipseWorkspaceRootFake extends EclipseContainerFake implements
    IWorkspaceRoot {

    protected EclipseWorkspaceRootFake(File wrappedFile) {
        super(wrappedFile);
    }

    public IContainer[] findContainersForLocation(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IContainer[] findContainersForLocationURI(URI uri) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IContainer[] findContainersForLocationURI(URI uri, int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFile[] findFilesForLocation(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFile[] findFilesForLocationURI(URI uri) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFile[] findFilesForLocationURI(URI uri, int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IContainer getContainerForLocation(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFile getFileForLocation(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProject getProject(String s) {
        return de.fu_berlin.inf.dpp.test.fakes.EclipseProjectFake.getProject(wrappedFile.getPath() + "/" + s);
    }

    public IProject[] getProjects() {
        List<File> wrappedProjectFiles = newArrayList();
        for (File file : wrappedFile.listFiles()) {
            if (file.isDirectory()) {
                wrappedProjectFiles.add(file);
            }
        }

        IProject[] result = new IProject[wrappedProjectFiles.size()];
        for (int i = 0; i < wrappedProjectFiles.size(); i++) {
            result[i] = de.fu_berlin.inf.dpp.test.fakes.EclipseProjectFake.getProject(wrappedProjectFiles.get(i)
                .getPath());
        }

        return result;
    }

    public IProject[] getProjects(int i) {
        return getProjects();
    }
}
