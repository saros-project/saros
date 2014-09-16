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

package de.fu_berlin.inf.dpp.core.workspace;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

import java.io.File;
import java.io.IOException;

public interface IWorkspace {
    public static final int AVOID_UPDATE = 1;

    void run(IWorkspaceRunnable procedure, IProgressMonitor monitor)
        throws OperationCanceledException, IOException;

    void run(IWorkspaceRunnable procedure, IWorkspaceRoot root, int mode,
        IProgressMonitor monitor)
        throws OperationCanceledException, IOException;

    IWorkspaceRoot getRoot();

    IWorkspaceDescription getDescription();

    void setDescription(IWorkspaceDescription description) throws IOException;

    File getPath();

    IPathFactory getPathFactory();

}
