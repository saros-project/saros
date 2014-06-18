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

package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.monitor.ISubMonitor;

/**
 * A dummy SubProgressBar implementation
 */
//todo: remove it and use de.fu_berlin.inf.dpp.monitoring.IProgressMonitor in all IntelliJ classes
//TODO: Add SubMonitor class to the core
public class SubProgressBar extends MonitorProgressBar implements ISubMonitor {
    private MonitorProgressBar main;
    private int subProgress = 0;

    /**
     * Creates a dummy sub progress-bar inside the MonitorProgressBar
     *
     * @param main the main progress bar this dummy resides in
     */
    public SubProgressBar(MonitorProgressBar main) {
        super(main.display);
        this.main = main;
    }

    /**
     * Creates a dummy sub progress-bar inside the DisplayContainer
     *
     * @param display
     */
    public SubProgressBar(DisplayContainer display) {
        super(display);
    }

    @Override
    public ISubMonitor newChild(int id) {
        return this;
    }

    @Override
    public IProgressMonitor getMain() {
        return main;
    }

    @Override
    public IProgressMonitor newChildMain(int progress) {
        this.subProgress = progress;
        return this;
    }

    @Override
    public IProgressMonitor newChildMain(int progress, int mode) {
        this.subProgress = progress;
        return this;
    }

    @Override
    public ISubMonitor newChild(int progress, int mode) {
        this.subProgress = progress;
        return this;
    }

    /**
     * Checks whether the progress the main progress bar or this progress bar
     * was cancelled.
     */
    public boolean isCanceled() {
        return getMain() != null ?
            getMain().isCanceled() || isCanceled :
            isCanceled;
    }

    @Override
    public void done() {
        if (subProgress < MAX_VALUE) {
            main.setProgress(subProgress);
        }

        display.setInfo(main.info);
    }

}
