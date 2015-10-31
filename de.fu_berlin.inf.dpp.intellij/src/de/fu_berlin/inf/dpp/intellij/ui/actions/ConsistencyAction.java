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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import com.intellij.openapi.application.ApplicationManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.MonitorProgressBar;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgressFrame;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Performs project recovery, when inconsistency was detected.
 */
public class ConsistencyAction extends AbstractSarosAction {
    public static final String NAME = "consistency";

    @Override
    public String getActionName() {
        return NAME;
    }

    public ConsistencyAction(ConsistencyWatchdogClient watchdogClient) {
        this.watchdogClient = watchdogClient;
    }

    private ConsistencyWatchdogClient watchdogClient;

    /**
     * This method starts
     * {@link ConsistencyWatchdogClient#runRecovery(IProgressMonitor)}.
     */
    @Override
    public void execute() {
        LOG.debug("user activated CW recovery.");

        final ProgressFrame progress = new ProgressFrame("Consistency action");
        progress.setFinishListener(new MonitorProgressBar.FinishListener() {
            @Override
            public void finished() {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        actionPerformed();
                    }
                });
            }
        });

        ThreadUtils.runSafeAsync(LOG, new Runnable() {
            @Override
            public void run() {
                progress.beginTask(
                    Messages.ConsistencyAction_progress_perform_recovery,
                    IProgressMonitor.UNKNOWN);
                watchdogClient.runRecovery(progress);
            }
        });
    }
}
