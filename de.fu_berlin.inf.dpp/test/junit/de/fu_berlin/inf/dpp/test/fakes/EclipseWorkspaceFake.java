package de.fu_berlin.inf.dpp.test.fakes;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.submonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.resources.WorkspaceDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.WorkspaceLock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IWorkspace}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionality which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * To get an initialized workspace you should use
 * {@link EclipseWorkspaceFakeFacade}.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */

public class EclipseWorkspaceFake implements IWorkspace {

    private File wrappedFile;

    private EclipseWorkspaceFake(File wrappedFile) {
        this.wrappedFile = wrappedFile;
    }

    public static EclipseWorkspaceFake getMockWorkspace(String path) {
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new EclipseWorkspaceFake(new File(path));
    }

    public void addResourceChangeListener(
        IResourceChangeListener iResourceChangeListener) {
        // do nothing...
    }

    public void addResourceChangeListener(
        IResourceChangeListener iResourceChangeListener, int i) {
        // do nothing...
    }

    public ISavedState addSaveParticipant(Plugin plugin,
        ISaveParticipant iSaveParticipant) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void build(int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void checkpoint(boolean b) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProject[][] computePrerequisiteOrder(IProject[] iProjects) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ProjectOrder computeProjectOrder(IProject[] iProjects) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus copy(IResource[] iResources, IPath iPath, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus copy(IResource[] iResources, IPath iPath, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus delete(IResource[] iResources, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus delete(IResource[] iResources, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void deleteMarkers(IMarker[] iMarkers) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void forgetSavedTree(String s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectNatureDescriptor[] getNatureDescriptors() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectNatureDescriptor getNatureDescriptor(String s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SuppressWarnings("rawtypes")
    public Map getDanglingReferences() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IWorkspaceDescription getDescription() {
        return new WorkspaceDescription(wrappedFile.getName());
    }

    public IWorkspaceRoot getRoot() {
        return new EclipseWorkspaceRootFake(wrappedFile);
    }

    public IResourceRuleFactory getRuleFactory() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ISynchronizer getSynchronizer() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isAutoBuilding() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isTreeLocked() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectDescription loadProjectDescription(IPath iPath)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectDescription loadProjectDescription(InputStream inputStream)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus move(IResource[] iResources, IPath iPath, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus move(IResource[] iResources, IPath iPath, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectDescription newProjectDescription(String s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void removeResourceChangeListener(
        IResourceChangeListener iResourceChangeListener) {
        // do nothing...
    }

    public void removeSaveParticipant(Plugin plugin) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void run(IWorkspaceRunnable iWorkspaceRunnable,
        ISchedulingRule iSchedulingRule, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        iWorkspaceRunnable.run(submonitor());
    }

    public void run(IWorkspaceRunnable iWorkspaceRunnable,
        IProgressMonitor iProgressMonitor) throws CoreException {
        iWorkspaceRunnable.run(submonitor());
    }

    public IStatus save(boolean b, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDescription(IWorkspaceDescription iWorkspaceDescription)
        throws CoreException {
        // do nothing
    }

    @Deprecated
    public void setWorkspaceLock(WorkspaceLock workspaceLock) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public String[] sortNatureSet(String[] strings) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateEdit(IFile[] iFiles, Object o) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateLinkLocation(IResource iResource, IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateLinkLocationURI(IResource iResource, URI uri) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateName(String s, int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateNatureSet(String[] strings) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validatePath(String s, int i) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateProjectLocation(IProject iProject, IPath iPath) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateProjectLocationURI(IProject iProject, URI uri) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IPathVariableManager getPathVariableManager() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class aClass) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ISavedState addSaveParticipant(String pluginId,
        ISaveParticipant participant) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFilterMatcherDescriptor getFilterMatcherDescriptor(
        String filterMatcherId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IFilterMatcherDescriptor[] getFilterMatcherDescriptors() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void removeSaveParticipant(String pluginId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IStatus validateFiltered(IResource resource) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
