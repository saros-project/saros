/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.vcs;

import de.fu_berlin.inf.dpp.activities.VCSActivity;
import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.exceptions.TeamException;
import de.fu_berlin.inf.dpp.core.invitation.FileList;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

/**
 * Interface to an adapter for a Version Control System (Team Provider).
 *
 * @author haferburg
 */
// TODO Maybe change to VCProject implements IAdaptable? Make connect static.
public abstract class VCSAdapter {
    protected static final Logger log = Logger.getLogger(VCSAdapter.class);
    @Inject
    private static IVCSFactory vcsFactory;
    protected IRepositoryProviderType provider;

    public VCSAdapter(IRepositoryProviderType provider) {
        this.provider = provider;
    }

    /**
     * Determine the repository provider of the project and return the
     * corresponding {@link VCSAdapter}. The method will return
     * <code>null</code> if the project is not under version control, or if no
     * <code>VCSAdapter</code> was found for the repository provider used.
     *
     * @param project
     * @return
     */
    public static VCSAdapter getAdapter(IProject project) {
        if (vcsFactory == null) {
            return null;
        }

        IRepositoryProvider provider = vcsFactory.getProvider(project);

        if (!provider.isShared(project)) {
            return null;
        }

        return getAdapter(provider.getID());
    }

    public static VCSAdapter getAdapter(String identifier) {
        if (identifier == null) {
            return null;
        }

        return vcsFactory.getAdapter(identifier);
    }

    /**
     * @param resource
     * @return True iff the resource is under VC.
     */
    public abstract boolean isManaged(IResource resource);

    /**
     * @param resource
     * @return True iff the resource is in a project that's under VC.
     */
    public abstract boolean isInManagedProject(IResource resource);

    /**
     * @param resource
     * @return The identifier of the resource's Team Provider.
     */
    public String getProviderID(IResource resource) {
        IProject project = resource.getProject();
        IRepositoryProvider provider = vcsFactory.getProvider(project);
        String vcsIdentifier = provider.getID();

        return vcsIdentifier;
    }

    /**
     * @param resource
     * @return The revision of the resource as a String, or null.
     */
    public String getRevisionString(IResource resource) {
        ISubscriber subscriber = provider.getSubscriber();
        ISyncInfo syncInfo;
        try {
            syncInfo = subscriber.getSyncInfo(resource);
        } catch (TeamException e) {
            undocumentedException(e);
            return null;
        }
        if (syncInfo == null) {
            return null;
        }
        return syncInfo.getLocalContentIdentifier();
    }

    /**
     * @param resource
     * @return The URL of the repository root of this resource as a String, or
     * null.
     */
    public abstract String getRepositoryString(IResource resource);

    /**
     * @param resource
     * @return The URL of the remote resource in the repository, or null.
     */
    public abstract String getUrl(IResource resource);

    /**
     * Checks out the project specified by the {@link FileList} as a new project
     * under the provided name.
     *
     * @param newProjectName
     * @param fileList
     * @param monitor
     * @return The newly created project.
     */
    public abstract IProject checkoutProject(String newProjectName,
        FileList fileList, IProgressMonitor monitor)
        throws OperationCanceledException;

    /**
     * Updates the file to the specified revision.
     *
     * @param resource
     * @param targetRevision
     * @param monitor        must not be null.
     */
    public abstract void update(IResource resource, String targetRevision,
        IProgressMonitor monitor);

    /**
     * Switches the resource to the specified URL and revision.
     *
     * @param monitor
     */
    public abstract void switch_(IResource resource, String url,
        String revision, IProgressMonitor monitor);

    /**
     * Reverts the local changes to the resource.
     *
     * @param monitor
     */
    public abstract void revert(IResource resource, ISubMonitor monitor);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the actual revision of the file (last changed revision), uniquely
     * identifying the resource in the repository. E.g. if a file was last
     * changed in revision 121, HEAD is 127, and we do an "svn update -r 124",
     * then this method returns revision 121. We need this revision to detect
     * changes: We get sync changed events only if the actual revision of a
     * resource changes.
     *
     * @param resource
     * @return
     */
    public abstract VCSResourceInfo getResourceInfo(IResource resource);

    /**
     * Returns VCS specific information for the resource. For SVN, it returns
     * the revision that was used to get this file. E.g. if a file was last
     * changed in revision 121, HEAD is 127, and we do an "svn update -r 124",
     * then this method returns revision 124. We need this revision to replicate
     * changes: If we used the actual revision in commands, it's possible that
     * the URL changes.
     *
     * @param resource
     * @return
     */
    public abstract VCSResourceInfo getCurrentResourceInfo(IResource resource);

    /**
     * Connects the project to the directory in the repository.
     *
     * @param project
     * @param repositoryRoot
     * @param directory
     * @param progress       may be null.
     */
    public abstract void connect(IProject project, String repositoryRoot,
        String directory, IProgressMonitor progress);

    /**
     * Disconnects the project from the repository.
     *
     * @param project
     * @param deleteContent
     * @param progress      may be null.
     */
    public abstract void disconnect(IProject project, boolean deleteContent,
        IProgressMonitor progress);

    /**
     * Returns true if there is a folder like e.g. SVN's .svn for the project.
     * Such a folder might exists even when the project is not currently
     * connected to the Team provider.
     */
    public abstract boolean hasLocalCache(IProject project);

    /**
     * It is unclear under which circumstances this exception is thrown.
     */
    protected void undocumentedException(Exception e) {
        log.error("Undocumented exception", e);
    }

    /**
     * Determine and instantiate the corresponding {@link VCSAdapter} for the
     * provided identifier.<br>
     *
     * @param identifier
     * @return
     * @see RepositoryProvider#getID()
     */

    public abstract VCSActivity getUpdateActivity(ISarosSession sarosSession,
        IResource resource);

    public abstract VCSActivity getSwitchActivity(ISarosSession sarosSession,
        IResource resource);

}