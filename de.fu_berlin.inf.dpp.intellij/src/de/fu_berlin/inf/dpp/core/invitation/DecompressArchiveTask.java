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

package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//TODO: Clean up when movin eclipse class to core
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
     * @param file               Zip file containing the compressed data
     * @param idToProjectMapping map containing the id to project mapping (see also
     *                           {@link ISarosSession#getProjectID(de.fu_berlin.inf.dpp.filesystem.IProject)}
     * @param monitor            monitor that is used for progress report and cancellation or
     *                           <code>null</code> to use the monitor provided by the
     *                           {@link #run(IProgressMonitor)} method
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
    public void run(IProgressMonitor monitor)
        throws OperationCanceledException, IOException {
        if (this.monitor != null) {
            monitor = this.monitor;
        }

        ZipFile zipFile = null;

        try {

            zipFile = new ZipFile(file);

            ISubMonitor subMonitor = monitor
                .convert("Unpacking archive file to workspace", zipFile.size());

            for (Enumeration<? extends ZipEntry> entries = zipFile
                .entries(); entries.hasMoreElements(); ) {

                final ZipEntry entry = entries.nextElement();

                final String entryName = entry.getName();

                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                final int delimiterIdx = entry.getName().indexOf(delimiter);

                if (delimiterIdx == -1) {
                    LOG.warn("skipping zip entry " + entryName
                        + ", entry is not valid");

                    subMonitor.worked(1);
                    continue;
                }

                final String id = entryName.substring(0, delimiterIdx);

                final String path = entryName
                    .substring(delimiterIdx + 1, entryName.length());

                final IProject project = idToProjectMapping.get(id);

                if (project == null) {
                    LOG.warn("skipping zip entry " + entryName
                        + ", unknown project id: " + id);

                    subMonitor.worked(1);
                    continue;
                }

                final IFile file = project.getFile(path);

                /*
                 * do not use FileUtils because it will remove read-only access
                 * which might not what the user want
                 */
                createFoldersForFile(file);

                subMonitor.subTask("decompressing: " + path);

                final InputStream in = zipFile.getInputStream(entry);

                if (!file.exists()) {
                    file.create(in, true);
                } else {
                    file.setContents(in, true, true);
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("file written to disk: " + path);
                }
            }

        } catch (IOException e) {
            LOG.error("failed to unpack archive", e);

            throw e;
        } finally {
            monitor.subTask("");

            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException e) {
                LOG.warn(
                    "failed to close zip file " + zipFile.getName() + " : " + e
                        .getMessage()
                );
                throw e;
            }

            monitor.done();
        }
    }

    private void createFoldersForFile(IFile file) throws IOException {
        List<IFolder> parents = new ArrayList<IFolder>();

        IContainer parent = file.getParent();

        while (parent != null && parent.getType() == IResource.FOLDER) {
            if (parent.exists()) {
                break;
            }

            parents.add((IFolder) parent);
            parent = parent.getParent();
        }

        Collections.reverse(parents);

        for (IFolder folder : parents) {
            folder.create(false, true);
        }
    }
}
