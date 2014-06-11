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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// TODO Consolidate carefully with Saros/E's DecompressArchiveTask
public class DecompressTask implements IWorkspaceRunnable {

    private static final Logger LOG = Logger.getLogger(DecompressTask.class);

    private final ZipInputStream in;
    private final IProgressMonitor monitor;
    private final IProject project;

    private List<String> preventExt = Arrays
        .asList(new String[] { "iml", "ipr", "iws" });

    @Inject
    private IPathFactory pathFactory;

    /**
     * Creates a decompress task that can be executed by {@link de.fu_berlin.inf.dpp.core.workspace.IWorkspace#run}.
     * All necessary folders will be created on the fly and existing files will
     * be <b>overwritten without confirmation</b>.
     *
     * @param in      zip input stream providing the compressed data
     * @param project project to uncompress the data to
     * @param monitor monitor that is used for progress report and cancellation or
     *                <code>null</code> to use the monitor provided by the
     *                {@link #run(IProgressMonitor)} method
     */
    public DecompressTask(ZipInputStream in, IProject project,
        IProgressMonitor monitor) {
        this.in = in;
        this.project = project;
        this.monitor = monitor;
    }

    public void setPathFactory(IPathFactory pathFactory) {
        this.pathFactory = pathFactory;
    }

    // TODO extract as much as possible even on some failures
    @Override
    public void run(IProgressMonitor monitor) throws IOException {
        if (this.monitor != null) {
            monitor = this.monitor;
        }

        // TODO calculate size for better progress

        ISubMonitor subMonitor = monitor
            .convert(monitor, "Unpacking archive file to workspace", 1);

        try {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (subMonitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                IPath path = pathFactory.fromString(entry.getName());
                IFile file = project.getFile(path);

                /*
                 * do not use FileUtils because it will removeAll read-only access
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

                if (!file.exists()) {
                    file.create(uncloseable, true);
                } else {
                    if (!preventExt
                        .contains(file.getFullPath().getFileExtension())) {
                        file.setContents(uncloseable, true, true);
                    }
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("file written to disk: " + path);
                }

                in.closeEntry();
            }

        } catch (IOException e) {
            LOG.error("failed to unpack archive", e);
            throw e;
        } finally {
            monitor.subTask("");
            IOUtils.closeQuietly(in);
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

    public void setPreventExt(String... extensions) {
        this.preventExt = Arrays.asList(extensions);
    }
}

