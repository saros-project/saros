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
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 * </p>
 * TODO This should either be a Saros/Core interface or more adapted to IntelliJ
 */
public interface ISubMonitor extends IProgressMonitor {

    public static final int SUPPRESS_NONE = 1;
    public static final int SUPPRESS_ALL_LABELS = 2;

    void subTask(String name);

    void done();

    ISubMonitor newChild(int id);

    IProgressMonitor getMain();

    IProgressMonitor newChildMain(int progress);

    IProgressMonitor newChildMain(int progress, int mode);

    ISubMonitor newChild(int progress, int mode);

    boolean isCanceled();

    void setCanceled(boolean cancel);

    void setTaskName(String name);

    void beginTask(String taskName, int workTotal);

    void worked(int worked);
}
