package de.fu_berlin.inf.dpp.negotiation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Adler32;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList.MetaData;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * Offers two ways to create {@link FileList file lists}.
 * <p>
 * <li>Either an inexpensive one that rescans the whole project to gather meta
 * data:<br>
 * {@link #createFileList(IProject, List, IChecksumCache, VCSProvider, IProgressMonitor)}
 * </li>
 * <li>Or a cheap one which requires the caller to take care of the validity of
 * input data:<br>
 * {@link #createFileList(List)}</li>
 */
public class FileListFactory {

    private static final Logger LOG = Logger.getLogger(FileListFactory.class);

    private static final int BUFFER_SIZE = 32 * 1024;

    private IChecksumCache checksumCache;
    private IProgressMonitor monitor;

    private FileListFactory(IChecksumCache checksumCache,
        IProgressMonitor monitor) {
        this.checksumCache = checksumCache;
        this.monitor = monitor;

        if (this.monitor == null)
            this.monitor = new NullProgressMonitor();
    }

    public static FileList createFileList(IProject project,
        List<IResource> resources, IChecksumCache checksumCache,
        VCSProvider provider, IProgressMonitor monitor) throws IOException {

        FileListFactory fact = new FileListFactory(checksumCache, monitor);
        return fact.build(project, resources, provider);
    }

    /**
     * Creates a new file list from given paths. It does not compute checksums
     * or location information.
     * <p>
     * <b>Note:</b> This method does not check the input. The caller is
     * <b>responsible</b> for the <b>correct</b> input !
     * 
     * @param paths
     *            a list of paths that <b>refers</b> to <b>files</b> that should
     *            be added to this file list.
     */
    public static FileList createFileList(List<String> paths) {
        FileList list = new FileList();

        for (String path : paths)
            list.addPath(path);

        return list;
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

    private FileList build(IProject project, List<IResource> resources,
        VCSProvider provider) throws IOException {

        FileList list = new FileList();

        if (resources == null) {
            list.addEncoding(project.getDefaultCharset());
            resources = Arrays.asList(project.members());
        }

        addMembersToList(list, resources, provider);

        return list;
    }

    private void addMembersToList(final FileList list,
        final List<IResource> resources, final VCSProvider provider)
        throws IOException {

        if (resources.size() == 0)
            return;

        IProject project = null;

        if (provider != null) {
            project = resources.get(0).getProject();
            String providerID = provider.getID();

            list.setVcsProviderID(providerID);
            list.setVcsRepositoryRoot(provider.getRepositoryString(project));

            list.setVcsRepositoryRoot(provider.getCurrentResourceInfo(project));
            /*
             * FIXME we need to stop querying for VCS revisions the moment we
             * reach the first exception
             * 
             * Caused by:
             * org.tigris.subversion.svnclientadapter.SVNClientException:
             * org.apache.subversion.javahl.ClientException: The working copy
             * needs to be upgraded
             * 
             * which will significantly slow down the overall invitation
             * process. It doesn't make sense to check for other files. If there
             * is one resource that is not upgraded, this fails overall...
             */
        }

        Deque<IResource> stack = new LinkedList<IResource>();

        stack.addAll(resources);

        List<IFile> files = new LinkedList<IFile>();

        if (provider != null)
            monitor.subTask("Reading SVN revisions for shared files...");

        while (!stack.isEmpty()) {
            IResource resource = stack.pop();

            if (resource.isDerived() || !resource.exists())
                continue;

            String path = resource.getProjectRelativePath().toPortableString();

            if (list.contains(path))
                continue;

            VCSResourceInfo info = null;

            if (provider != null)
                info = provider.getCurrentResourceInfo(resource);

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
                monitor.subTask(file.getProject().getName() + ": "
                    + file.getName());

                MetaData data = list.getMetaData(file.getProjectRelativePath()
                    .toPortableString());

                Long checksum = null;

                if (checksumCache != null)
                    checksum = checksumCache.getChecksum(file);

                data.checksum = checksum == null ? checksum(file) : checksum;

                if (checksumCache != null) {
                    boolean isInvalid = checksumCache.addChecksum(file,
                        data.checksum);

                    if (isInvalid && checksum != null)
                        LOG.warn("calculated checksum on dirty data: "
                            + file.getFullPath());
                }

            } catch (IOException e) {
                LOG.error(e);
            }

            monitor.worked(1);
        }
    }

    /**
     * Calculate Adler32 checksum for given file.
     * <p>
     * TODO This method's signature is a temporary "anomaly" in this class, and
     * will be removed in future patches.
     * 
     * @return checksum of file
     * 
     * @throws IOException
     *             if checksum calculation has been failed.
     */
    private static long checksum(IFile file) throws IOException {

        InputStream in;
        try {
            in = file.getContents();
        } catch (IOException e) {
            throw new IOException("failed to calculate checksum", e);
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        Adler32 adler = new Adler32();

        int read;

        try {
            while ((read = in.read(buffer)) != -1)
                adler.update(buffer, 0, read);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return adler.getValue();
    }
}
