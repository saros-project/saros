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

package de.fu_berlin.inf.dpp.core.zip;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;

/*
 * TODO Which of the public methods actually need to be public? Should this
 * class implement IProgressMonitor?
 */
public class ZipProgressMonitor implements ZipListener {

    private final IProgressMonitor monitor;

    private int lastWorked = 0;
    private int workRemaining = 0;
    private boolean useFilesSize = true;

    private int align = 1;

    public ZipProgressMonitor(IProgressMonitor monitor, int fileCount,
        boolean useFilesSize) {

        this.monitor = monitor;
        this.useFilesSize = useFilesSize;

        workRemaining = fileCount;

        if (workRemaining <= 0) {
            workRemaining = IProgressMonitor.UNKNOWN;
        }

        if (useFilesSize) {
            workRemaining = 100;
        }

        beginTask("Compressing files...", workRemaining);
    }

    @Override
    public boolean update(String filename) {
        subTask("compressing file: " + filename);

        if (!useFilesSize && workRemaining != IProgressMonitor.UNKNOWN) {
            worked(1 - align);
            align = 0;
        }

        return isCanceled();
    }

    @Override
    public boolean update(long totalRead, long totalSize) {
        if (!useFilesSize || totalSize <= 0) {
            return isCanceled();
        }

        int worked = (int) ((totalRead * 100L) / totalSize);
        int workedDelta = worked - lastWorked;

        if (workedDelta > 0) {
            worked(workedDelta);
            lastWorked = worked;
        }
        return isCanceled();
    }

    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    public void setCanceled(boolean canceled) {
        monitor.setCanceled(canceled);
    }

    public void worked(int workedDelta) {
        monitor.worked(workedDelta);
    }

    public void beginTask(String title, int workRemaining) {
        monitor.beginTask(title, workRemaining);
    }

    public void subTask(String title) {
        monitor.subTask(title);
    }
}
