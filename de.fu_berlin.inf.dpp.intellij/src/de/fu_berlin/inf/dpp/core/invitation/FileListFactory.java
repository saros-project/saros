package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.NullProgressMonitor;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.core.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.core.vcs.VCSResourceInfo;
import de.fu_berlin.inf.dpp.filesystem.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static de.fu_berlin.inf.dpp.core.invitation.FileList.MetaData;

/**
 * Offers two ways to create {@link FileList}s, i.e. an inexpensive one, which
 * requires the caller to take care of the validity of input data (
 * {@link #createFileList(List) createFileList(List&lt;String&gt;)}), and an
 * expensive one, that rescans the whole project to gather all MetaData (
 * {@link #createFileList(IProject, List, IChecksumCache, boolean, IProgressMonitor)
 * createFileList(IProject, List&lt;IResource&gt;, ...)}).
 */
public class FileListFactory {

    private static final Logger LOG = Logger.getLogger(FileListFactory.class);

    private IChecksumCache checksumCache;
    private IProgressMonitor monitor;

    private FileListFactory(IChecksumCache checksumCache,
        IProgressMonitor monitor) {
        this.checksumCache = checksumCache;
        this.monitor = monitor;

        if (this.monitor == null) {
            this.monitor = new NullProgressMonitor();
        }
    }

    public static FileList createFileList(IProject project,
        List<IResource> resources, IChecksumCache checksumCache,
        boolean useVersionControl, IProgressMonitor monitor)
        throws IOException {

        FileListFactory fact = new FileListFactory(checksumCache, monitor);
        return fact.build(project, resources, useVersionControl);
    }

    /**
     * Creates a new file list from given paths. It does not compute checksums
     * or location information.
     *
     * @param paths a list of paths that <b>refers</b> to <b>files</b> that should
     *              be added to this file list.
     * @NOTE This method does not check the input. The caller is
     * <b>responsible</b> for the <b>correct</b> input !
     */
    public static FileList createFileList(List<String> paths) {
        FileList list = new FileList(false);

        for (String path : paths) {
            list.addPath(path);
        }

        return list;
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

    private FileList build(IProject project, List<IResource> resources,
        boolean useVersionControl) throws IOException {

        FileList list = new FileList(useVersionControl);

        if (resources == null) {
            list.addEncoding(project.getDefaultCharset());
            resources = Arrays.asList(project.members());
        }

        addMembersToList(list, resources);

        return list;
    }

    private void addMembersToList(FileList list, List<IResource> resources)
        throws IOException {

        if (resources.size() == 0) {
            return;
        }

        IProject project = null;
        VCSAdapter vcs = null;

        if (list.useVersionControl()) {
            project = resources.get(0).getProject();

            vcs = VCSAdapter.getAdapter(project);

            if (vcs != null) {
                String providerID = vcs.getProviderID(project);

                list.setVcsProviderID(providerID);
                list.setVcsRepositoryRoot(
                    vcs.getRepositoryString(project));
                list.setVcsRepositoryRoot(
                    vcs.getCurrentResourceInfo(project));
                /*
                 * FIXME we need to stop querying for VCS revisions the moment
                 * we reach the first exception
                 * 
                 * Caused by:
                 * org.tigris.subversion.svnclientadapter.SVNClientException:
                 * org.apache.subversion.javahl.ClientException: The working
                 * copy needs to be upgraded
                 * 
                 * which will significantly slow down the overall invitation
                 * process. It doesn't make sense to check for other files. If
                 * there is one resource that is not upgraded, this fails
                 * overall...
                 */
            }
        }

        Deque<IResource> stack = new LinkedList<IResource>();

        stack.addAll(resources);

        List<IFile> files = new LinkedList<IFile>();

        monitor.subTask("Reading SVN revisions for shared files...");
        while (!stack.isEmpty()) {
            IResource resource = stack.pop();

            if (resource.isDerived() || !resource.exists()) {
                continue;
            }

            String path = resource.getProjectRelativePath().toPortableString();

            if (list.contains(path)) {
                continue;
            }

            VCSResourceInfo info = null;

            if (vcs != null) {
                info = vcs.getCurrentResourceInfo(resource);
            }

            assert !list.useVersionControl() || (project != null && project
                .equals(resource.getProject()));

            MetaData data = null;

            switch (resource.getType()) {
            case IResource.FILE:
                files.add((IFile) resource);
                data = new MetaData();
                data.vcsInfo = info;
                list.addPath(path, data, false);
                list.addEncoding(((IFile) resource).getCharset());
                break;
            case IResource.FOLDER:
                stack.addAll(Arrays.asList(((IFolder) resource).members()));

                if (info != null) {
                    data = new MetaData();
                    data.vcsInfo = info;
                }
                list.addPath(path, data, true);
                break;
            }
        }

        monitor.beginTask("Calculating checksums...", files.size());

        for (IFile file : files) {
            try {
                monitor.subTask(
                    file.getProject().getName() + ": " + file.getName());

                MetaData data = list.getMetaData(
                    file.getProjectRelativePath().toPortableString());

                Long checksum = null;

                /** {@link IChecksumCache} **/
                String path = file.getFullPath().toPortableString();

                if (checksumCache != null) {
                    checksum = checksumCache.getChecksum(path);
                }

                data.checksum =
                    checksum == null ? FileUtils.checksum(file) : checksum;

                if (checksumCache != null) {
                    boolean isInvalid = checksumCache
                        .addChecksum(path, data.checksum);

                    if (isInvalid && checksum != null) {
                        LOG.warn("calculated checksum on dirty data: " + file
                            .getFullPath());
                    }
                }

            } catch (IOException e) {
                LOG.error(e);
            }

            monitor.worked(1);
        }
    }
}
