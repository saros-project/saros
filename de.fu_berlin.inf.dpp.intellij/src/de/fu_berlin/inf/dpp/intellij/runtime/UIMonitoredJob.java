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

package de.fu_berlin.inf.dpp.intellij.runtime;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.IStatus;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgressFrame;

/**
 * Class designed to start long lasting job with progress indicator
 */
public abstract class UIMonitoredJob extends Thread {

    private IProgressMonitor monitor;

    /**
     * Creates a new UIMonitoredJob with the given name monitored by monitor.
     *
     * @param name progress window name
     */
    public UIMonitoredJob(String name, IProgressMonitor monitor) {
        super(name);
        if (monitor == null) {
            this.monitor = new ProgressFrame();
        } else {
            this.monitor = monitor;
        }
        this.monitor.setTaskName(name);
    }

    /**
     * Creates job with named progress window
     *
     * @param name progress window name
     */
    public UIMonitoredJob(final String name) {
        this(name, null);
    }

    public void schedule() {
        start();
    }

    @Override
    public final void run() {
        run(monitor);
    }

    /**
     * Implement job business logic here.
     * IProgressMonitor is passed internally.
     * Implementation is responsible to pass information about progress for progress monitor
     *
     * @param monitor
     * @return
     */
    protected abstract IStatus run(IProgressMonitor monitor);

}
