package de.fu_berlin.inf.dpp.test.fakes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentTypeMatcher;

/**
 * Fake-Implementation of {@link org.eclipse.core.resources.IProject}.
 * 
 * Wrappes a {@link java.io.File} to delegate the the most functionality.
 * 
 * If you call a functionality which is not implemented you will get a
 * {@link UnsupportedOperationException}.
 * 
 * For getting an instance of that implementation you need to call
 * {@link EclipseWorkspaceRootFake#getProject(String)} on an appropriate
 * instance of an EclipseWorkspaceRootFake.
 * 
 * @see de.fu_berlin.inf.dpp.test.util.EclipseWorkspaceFakeFacadeTest
 * @author cordes
 */
public class EclipseProjectFake extends EclipseContainerFake implements
    IProject {

    private EclipseProjectFake(File wrappedFile) {
        super(wrappedFile);
        project = this;
    }

    protected static EclipseProjectFake getProject(String path) {
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(path);
        Assert.isTrue(file.isDirectory(),
            "Wrapped file for Project should be directory!");
        return new EclipseProjectFake(file);
    }

    @SuppressWarnings("rawtypes")
    public void build(int i, String s, Map map,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void build(int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void close(IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(IProjectDescription iProjectDescription,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(IProjectDescription iProjectDescription, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectDescription getDescription() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProjectNature getNature(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IPath getPluginWorkingLocation(IPluginDescriptor iPluginDescriptor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IPath getWorkingLocation(String s) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProject[] getReferencedProjects() throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public IProject[] getReferencingProjects() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean hasNature(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isNatureEnabled(String s) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isOpen() {
        return true;
    }

    public void move(IProjectDescription iProjectDescription, boolean b,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void open(int i, IProgressMonitor iProgressMonitor)
        throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void open(IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDescription(IProjectDescription iProjectDescription,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void setDescription(IProjectDescription iProjectDescription, int i,
        IProgressMonitor iProgressMonitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public IPath getProjectRelativePath() {
        return new Path("");
    }

    @Override
    public int getType() {
        return PROJECT;
    }

    protected File getWrappedFile() {
        return wrappedFile;
    }

    public void loadSnapshot(int options, URI snapshotLocation,
        IProgressMonitor monitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void saveSnapshot(int options, URI snapshotLocation,
        IProgressMonitor monitor) throws CoreException {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
