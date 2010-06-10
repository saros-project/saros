package de.fu_berlin.inf.dpp.vcs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.FileList;

public interface VCSAdapter {

    public abstract boolean isInManagedProject(IResource resource);

    public abstract String getProviderID(IResource resource);

    public abstract boolean isManaged(IResource resource);

    public abstract String getRevisionString(IResource resource);

    public abstract String getRepositoryString(IResource resource);

    public abstract IProject checkoutProject(String newProjectName,
        FileList fileList, SubMonitor monitor);

    public abstract String getProjectPath(IResource resource);

}