package de.fu_berlin.inf.dpp.invitation;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
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

public class DecompressTask implements IWorkspaceRunnable {

    private static final Logger LOG = Logger.getLogger(DecompressTask.class);

    private final ZipInputStream in;
    private final IProgressMonitor monitor;
    private final IProject project;

    /**
     * Creates a decompress task that can be executed by {@link IWorkspace#run}.
     * All necessary folders will be created on the fly and existing files will
     * be <b>overwritten without confirmation</b>.
     * 
     * @param in
     *            zip input stream providing the compressed data
     * @param project
     *            project to uncompress the data to
     * @param monitor
     *            monitor that is used for progress report and cancellation or
     *            <code>null</code> to use the monitor provided by the
     *            {@link #run(IProgressMonitor)} method
     */
    public DecompressTask(ZipInputStream in, IProject project,
        IProgressMonitor monitor) {
        this.in = in;
        this.project = project;
        this.monitor = monitor;
    }

    // TODO extract as much as possible even on some failures
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (this.monitor != null)
            monitor = this.monitor;

        // TODO calculate size for better progress

        SubMonitor subMonitor = SubMonitor.convert(monitor,
            "Unpacking archive file to workspace", 1);

        try {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {

                if (subMonitor.isCanceled())
                    throw new OperationCanceledException();

                IPath path = Path.fromPortableString(entry.getName());
                IFile file = project.getFile(path);

                /*
                 * do not use FileUtils because it will remove read-only access
                 * which might not what the user want
                 */
                createFoldersForFile(file);

                InputStream uncloseable = new FilterInputStream(in) {
                    @Override
                    public void close() throws IOException {
                        // prevent the ZipInputStream from being closed
                    }
                };

                subMonitor.subTask("decompressing: " + path);

                if (!file.exists())
                    file.create(uncloseable, true,
                        subMonitor.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));
                else
                    file.setContents(uncloseable, true, true,
                        subMonitor.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));

                if (LOG.isTraceEnabled())
                    LOG.trace("file written to disk: " + path);

                in.closeEntry();
            }

        } catch (IOException e) {
            LOG.error("failed to unpack archive", e);
            throw new CoreException(new org.eclipse.core.runtime.Status(
                IStatus.ERROR, Saros.SAROS, "failed to unpack archive", e));
        } finally {
            monitor.subTask("");
            IOUtils.closeQuietly(in);
            monitor.done();
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
