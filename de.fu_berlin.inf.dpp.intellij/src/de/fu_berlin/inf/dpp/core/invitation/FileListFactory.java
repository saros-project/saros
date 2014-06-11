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

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;

import java.io.IOException;
import java.util.List;

// TODO Consolidate carefully, considerable difference with Saros/E counterpart
public class FileListFactory {

    public static FileList createFileList(IProject project,
        List<IResource> resources, IChecksumCache checksumCache,
        boolean useVersionControl, IProgressMonitor monitor)
        throws IOException {

        if (resources == null) {
            return new FileList(project, checksumCache, useVersionControl,
                monitor);
        }

        return new FileList(resources, checksumCache, useVersionControl,
            monitor);
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
    public static FileList createPathFileList(List<IPath> paths) {
        return new FileList(paths);
    }

    public static FileList createEmptyFileList() {
        return new FileList();
    }

}

