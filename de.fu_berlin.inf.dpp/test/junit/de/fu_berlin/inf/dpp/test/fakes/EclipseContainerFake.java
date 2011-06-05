package de.fu_berlin.inf.dpp.test.fakes;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IContainer}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionallity which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
public class EclipseContainerFake extends EclipseResourceFake implements
    IContainer, IFolder {

    protected EclipseContainerFake(File wrappedFile) {
        super(wrappedFile);
    }

    public boolean exists(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResource findMember(String s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResource findMember(String s, boolean b) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResource findMember(IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResource findMember(IPath iPath, boolean b) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getDefaultCharset() throws CoreException {
        return "UTF-8";
    }

    public String getDefaultCharset(boolean b) throws CoreException {
        return "UTF-8";
    }

    public IFile getFile(IPath iPath) {
        return getFile(iPath.toPortableString());
    }

    public IFolder getFolder(IPath iPath) {
        String path = wrappedFile.getPath() + "/" + iPath;
        EclipseFolderFake result = new EclipseFolderFake(new File(path));
        result.project = project;
        return result;
    }

    public IResource[] members() throws CoreException {

        Collection<File> files = FileUtils.listFiles(project.wrappedFile,
            new String[] { "java" }, true);
        IResource[] result = new IResource[files.size()];
        int i = 0;
        for (File file : files) {
            result[i] = EclipseFileFake.getMockFile(file, project);
            i++;
        }
        return result;
    }

    public IResource[] members(boolean b) throws CoreException {
        return members();
    }

    public IResource[] members(int i) throws CoreException {
        return members();
    }

    public IFile[] findDeletedMembersWithHistory(int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDefaultCharset(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDefaultCharset(String s, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(boolean b, boolean b1, IProgressMonitor iProgressMonitor)
        throws CoreException {
        try {
            FileUtils.forceMkdir(wrappedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void create(int i, boolean b, IProgressMonitor iProgressMonitor)
        throws CoreException {
        create(false, false, submonitor());
    }

    public void createLink(IPath iPath, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void createLink(URI uri, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void delete(boolean b, boolean b1, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFile getFile(String path) {
        return EclipseFileFake.getMockFile(new Path(path), project);
    }

    public IFolder getFolder(String s) {
        return getFolder(new Path(s));
    }

    public void move(IPath iPath, boolean b, boolean b1,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public URI getLocationURI() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResourceFilterDescription createFilter(int type,
        FileInfoMatcherDescription matcherDescription, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResourceFilterDescription[] getFilters() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
