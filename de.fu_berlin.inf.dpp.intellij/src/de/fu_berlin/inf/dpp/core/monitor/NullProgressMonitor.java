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

package de.fu_berlin.inf.dpp.core.monitor;

/**
 * This is a dummy implementation to ease the copy-paste-adapt process of
 * creating Saros/I out of Saros/E.
 * <p/>
 * TODO Check whether this actually necessary
 */
public class NullProgressMonitor implements IProgressMonitor {
    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setCanceled(boolean cancel) {
    }

    @Override
    public void worked(int delta) {
    }

    @Override
    public void subTask(String remaingTime) {
     }

    @Override
    public void setTaskName(String name) {
    }

    @Override
    public void done() {
    }

    @Override
    public void beginTask(String taskName, String type) {
     }

    @Override
    public void beginTask(String taskNam, int size) {
    }

    @Override
    public void internalWorked(double work) {
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor) {
        return new NullProgressSubMonitor(this);
    }

    @Override
    public ISubMonitor convert(IProgressMonitor monitor, String title,
        int progress) {
        return new NullProgressSubMonitor(this);
    }
}
