package de.fu_berlin.inf.dpp.test.stubs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

// TODO: Actually the IFile interface is not intended to be implemented by clients.
/**
 * FileStub provides a mockup implementation for eclipse's IFile
 */
public class FileStub implements IFile {

    private IProject project = null;
    private IPath path;
    private String content;

    private boolean derived = false;

    public FileStub(IProject project, String path, String content) {
        this(path, content);
        this.project = project;
    }

    public FileStub(String path, String content) {
        this.path = new Path(path);
        this.content = content;
    }

    public IPath getProjectRelativePath() {
        return path;
    }

    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FileStub))
            return false;
        FileStub other = (FileStub) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }

    public boolean exists() {
        return true;
    }

    public void appendContents(InputStream source, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void appendContents(InputStream source, boolean force,
        boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void create(InputStream source, boolean force,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void create(InputStream source, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void createLink(IPath localLocation, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void createLink(URI location, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void delete(boolean force, boolean keepHistory,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public String getCharset() throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public String getCharset(boolean checkImplicit) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public String getCharsetFor(Reader reader) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IContentDescription getContentDescription() throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public InputStream getContents(boolean force) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public int getEncoding() throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IPath getFullPath() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IFileState[] getHistory(IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public String getName() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void move(IPath destination, boolean force, boolean keepHistory,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setCharset(String newCharset) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setCharset(String newCharset, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setContents(InputStream source, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setContents(IFileState source, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setContents(InputStream source, boolean force,
        boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setContents(IFileState source, boolean force,
        boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void accept(IResourceVisitor visitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void accept(IResourceProxyVisitor visitor, int memberFlags)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void accept(IResourceVisitor visitor, int depth,
        boolean includePhantoms) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void accept(IResourceVisitor visitor, int depth, int memberFlags)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void clearHistory(IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void copy(IPath destination, boolean force, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void copy(IPath destination, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void copy(IProjectDescription description, boolean force,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void copy(IProjectDescription description, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IMarker createMarker(String type) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IResourceProxy createProxy() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void delete(boolean force, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void delete(int updateFlags, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void deleteMarkers(String type, boolean includeSubtypes, int depth)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IMarker findMarker(long id) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public int findMaxProblemSeverity(String type, boolean includeSubtypes,
        int depth) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public String getFileExtension() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public long getLocalTimeStamp() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IPath getLocation() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public URI getLocationURI() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IMarker getMarker(long id) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public long getModificationStamp() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IContainer getParent() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @SuppressWarnings("rawtypes")
    public Map getPersistentProperties() throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public String getPersistentProperty(QualifiedName key) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IProject getProject() {
        return project;
    }

    public IPath getRawLocation() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public URI getRawLocationURI() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public ResourceAttributes getResourceAttributes() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @SuppressWarnings("unchecked")
    public Map getSessionProperties() throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public Object getSessionProperty(QualifiedName key) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public int getType() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public IWorkspace getWorkspace() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isAccessible() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isDerived() {
        return derived;

    }

    public boolean isDerived(int options) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isHidden() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isLinked() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isLinked(int options) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isLocal(int depth) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isPhantom() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isSynchronized(int depth) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isTeamPrivateMember() {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void move(IPath destination, boolean force, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void move(IPath destination, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void move(IProjectDescription description, int updateFlags,
        IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void move(IProjectDescription description, boolean force,
        boolean keepHistory, IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void refreshLocal(int depth, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void revertModificationStamp(long value) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setDerived(boolean isDerived) throws CoreException {
        setDerived(isDerived, null);
    }

    public void setDerived(boolean isDerived, IProgressMonitor monitor)
        throws CoreException {
        derived = isDerived;
    }

    public void setHidden(boolean isHidden) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public long setLocalTimeStamp(long value) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setPersistentProperty(QualifiedName key, String value)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setReadOnly(boolean readOnly) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setResourceAttributes(ResourceAttributes attributes)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setSessionProperty(QualifiedName key, Object value)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void setTeamPrivateMember(boolean isTeamPrivate)
        throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public void touch(IProgressMonitor monitor) throws CoreException {
        throw new RuntimeException("Unexpected call to Stub");

    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean contains(ISchedulingRule rule) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isConflicting(ISchedulingRule rule) {
        throw new RuntimeException("Unexpected call to Stub");

    }

    public boolean isHidden(int options) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public boolean isTeamPrivateMember(int options) {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public IPathVariableManager getPathVariableManager() {
        throw new RuntimeException("Unexpected call to Stub");
    }

    public boolean isVirtual() {
        return false;
    }

}
