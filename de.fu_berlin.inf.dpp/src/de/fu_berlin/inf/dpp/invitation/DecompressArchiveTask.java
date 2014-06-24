package de.fu_berlin.inf.dpp.invitation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.session.ISarosSession;

public class DecompressArchiveTask implements IWorkspaceRunnable {

    private static final Logger LOG = Logger
        .getLogger(DecompressArchiveTask.class);

    private final File file;
    private final IProgressMonitor monitor;
    private final Map<String, IProject> idToProjectMapping;
    private final String delimiter;

    /**
     * Creates a decompress task for an archive file that can be executed by
     * {@link IWorkspace#run}. All necessary folders will be created on the fly.
     * </P> <b>Important:</b> Existing files will be <b>overwritten without
     * confirmation</b>!
     * 
     * @param file
     *            Zip file containing the compressed data
     * @param idToProjectMapping
     *            map containing the id to project mapping (see also
     *            {@link ISarosSession#getProjectID(de.fu_berlin.inf.dpp.filesystem.IProject)}
     * 
     * @param monitor
     *            monitor that is used for progress report and cancellation or
     *            <code>null</code> to use the monitor provided by the
     *            {@link #run(IProgressMonitor)} method
     */
    public DecompressArchiveTask(final File file,
        final Map<String, IProject> idToProjectMapping, final String delimiter,
        final IProgressMonitor monitor) {
        this.file = file;
        this.idToProjectMapping = idToProjectMapping;
        this.delimiter = delimiter;
        this.monitor = monitor;
    }

    // TODO extract as much as possible even on some failures
    /*
     * optional smoother progress ... use bytes written which will result in
     * better response if there exists big files in the archive
     */
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (this.monitor != null)
            monitor = this.monitor;

        ZipFile zipFile = null;

        try {

            zipFile = new ZipFile(file);

            final SubMonitor progress = SubMonitor.convert(monitor,
                "Unpacking archive file to workspace", zipFile.size());

            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
                .hasMoreElements();) {

                final ZipEntry entry = entries.nextElement();

                final String entryName = entry.getName();

                if (progress.isCanceled())
                    throw new OperationCanceledException();

                final int delimiterIdx = entry.getName().indexOf(delimiter);

                if (delimiterIdx == -1) {
                    LOG.warn("skipping zip entry " + entryName
                        + ", entry is not valid");

                    progress.worked(1);
                    continue;
                }

                final String id = entryName.substring(0, delimiterIdx);

                final IPath path = new Path(entryName.substring(
                    delimiterIdx + 1, entryName.length()));

                final IProject project = idToProjectMapping.get(id);

                if (project == null) {
                    LOG.warn("skipping zip entry " + entryName
                        + ", unknown project id: " + id);

                    progress.worked(1);
                    continue;
                }

                final IFile file = project.getFile(path);

                /*
                 * do not use FileUtils because it will remove read-only access
                 * which might not what the user want
                 */
                createFoldersForFile(file);

                progress.subTask("decompressing: " + path);

                final InputStream in = zipFile.getInputStream(entry);

                if (!file.exists())
                    file.create(in, true,
                        progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
                else
                    file.setContents(in, true, true,
                        progress.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));

                if (LOG.isTraceEnabled())
                    LOG.trace("file written to disk: " + path);
            }

        } catch (IOException e) {
            LOG.error("failed to unpack archive", e);
            throw new CoreException(new org.eclipse.core.runtime.Status(
                IStatus.ERROR, Saros.SAROS, "failed to unpack archive", e));
        } finally {
            if (monitor != null)
                monitor.done();

            try {
                if (zipFile != null)
                    zipFile.close();
            } catch (IOException e) {
                LOG.warn("failed to close zip file " + zipFile.getName()
                    + " : " + e.getMessage());
            }
        }
    }

    private void createFoldersForFile(IFile file) throws CoreException {
        List<IFolder> parents = new ArrayList<IFolder>();

        IContainer parent = file.getParent();

        while (parent != null && parent.getType() == IResource.FOLDER) {
            if (parent.exists())
                break;

            parents.add((IFolder) parent);
            parent = parent.getParent();
        }

        Collections.reverse(parents);

        for (IFolder folder : parents)
            folder.create(false, true, new NullProgressMonitor());
    }
}
