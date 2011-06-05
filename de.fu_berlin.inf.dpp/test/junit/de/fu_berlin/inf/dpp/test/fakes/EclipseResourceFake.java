package de.fu_berlin.inf.dpp.test.fakes;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;


/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IResource}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionallity which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
abstract public class EclipseResourceFake implements IResource {

    protected File wrappedFile;
    protected EclipseProjectFake project;

    protected EclipseResourceFake(File wrappedFile) {
        this.wrappedFile = wrappedFile;
    }

    public void accept(IResourceProxyVisitor iResourceProxyVisitor, int i)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void accept(IResourceVisitor iResourceVisitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void accept(IResourceVisitor iResourceVisitor, int i, boolean b)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void accept(IResourceVisitor iResourceVisitor, int i, int i1)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void clearHistory(IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void copy(IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void copy(IPath iPath, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void copy(IProjectDescription iProjectDescription, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void copy(IProjectDescription iProjectDescription, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IMarker createMarker(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IResourceProxy createProxy() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void delete(boolean b, IProgressMonitor iProgressMonitor)
        throws CoreException {
        FileUtils.deleteQuietly(wrappedFile);
    }

    public void delete(int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        FileUtils.deleteQuietly(wrappedFile);
    }

    public void deleteMarkers(String s, boolean b, int i) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean exists() {
        return wrappedFile.exists();
    }

    public IMarker findMarker(long l) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IMarker[] findMarkers(String s, boolean b, int i)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public int findMaxProblemSeverity(String s, boolean b, int i)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getFileExtension() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IPath getFullPath() {
        return getProjectRelativePath();
    }

    public long getLocalTimeStamp() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IPath getLocation() {
        return new Path(wrappedFile.getAbsolutePath());
    }

    public IMarker getMarker(long l) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public long getModificationStamp() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getName() {
        return wrappedFile.getName();
    }

    public IContainer getParent() {
        return new EclipseContainerFake(wrappedFile.getParentFile());
    }

    public Map getPersistentProperties() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String getPersistentProperty(QualifiedName qualifiedName)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProject getProject() {
        return project;
    }

    public IPath getProjectRelativePath() {
        String path = wrappedFile.getPath();
        path = path.replaceAll(project.getWrappedFile().getPath(), "");
        path = path.replaceFirst("/", "");
        return new Path(path);
    }

    public IPath getRawLocation() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public URI getRawLocationURI() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ResourceAttributes getResourceAttributes() {
        return new ResourceAttributes();
    }

    public Map getSessionProperties() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Object getSessionProperty(QualifiedName qualifiedName)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public int getType() {
        int result = -1;
        if (wrappedFile.isDirectory()) {
            result = FOLDER;
        } else if (wrappedFile.isFile()) {
            result = FILE;
        }
        return result;
    }

    public IWorkspace getWorkspace() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isAccessible() {
        return true;
    }

    public boolean isDerived() {
        return false;
    }

    public boolean isDerived(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isHidden() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isHidden(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isLinked() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isLinked(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isLocal(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isPhantom() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isReadOnly() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isSynchronized(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isTeamPrivateMember() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isTeamPrivateMember(int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void move(IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
        throws CoreException {
        IFile newFile = project.getFile(iPath);
        File newIOFile = new File(newFile.getLocation().toPortableString());
        if (wrappedFile.isDirectory() || newIOFile.isDirectory()) {
            try {
                FileUtils.moveDirectory(wrappedFile, newIOFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileUtils.moveFile(wrappedFile, newIOFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wrappedFile = newIOFile;
    }

    public void move(IPath iPath, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        move(iPath, false, iProgressMonitor);
    }

    public void move(IProjectDescription iProjectDescription, boolean b,
        boolean b1, IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void move(IProjectDescription iProjectDescription, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void refreshLocal(int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        // do nothing...
    }

    public void revertModificationStamp(long l) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDerived(boolean b) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setHidden(boolean b) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setLocal(boolean b, int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public long setLocalTimeStamp(long l) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setPersistentProperty(QualifiedName qualifiedName, String s)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setReadOnly(boolean b) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setResourceAttributes(ResourceAttributes resourceAttributes)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setSessionProperty(QualifiedName qualifiedName, Object o)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setTeamPrivateMember(boolean b) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void touch(IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Object getAdapter(Class aClass) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean contains(ISchedulingRule iSchedulingRule) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isConflicting(ISchedulingRule iSchedulingRule) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
