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

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Progress bar with autoincrement to use as COMPONENT in UI
 */

public class MonitorProgressBar implements IProgressMonitor {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 100;
    protected String info;
    protected DisplayContainer display;
    protected boolean isCanceled = false;

    private FinishListener finishListener;

    protected MonitorProgressBar(DisplayContainer display) {
        this.display = display;
    }

    protected MonitorProgressBar() {
        this.display = new DisplayContainer(
            new JProgressBar(MIN_VALUE, MAX_VALUE), new JLabel());
    }

    /**
     * Creates progress bar w/o additional information
     *
     * @param progressBar
     */
    public MonitorProgressBar(JProgressBar progressBar) {
        this(progressBar, null);
    }

    /**
     * Creates progress bar with additional information
     *
     * @param progressBar JProgressBar - progress information
     * @param infoLabel   JLabel - additional information
     */
    public MonitorProgressBar(JProgressBar progressBar, JLabel infoLabel) {
        this.display = new DisplayContainer(progressBar, infoLabel);
    }

    /**
     * Sets real progress to UI
     *
     * @param progress progress
     */
    public void setProgress(int progress) {
        this.display.setProgress(progress);
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @Override
    public void setCanceled(boolean cancel) {
        this.isCanceled = cancel;
        done();
    }

    @Override
    public void worked(int delta) {
        setProgress(this.display.getProgressBar().getValue() + delta);
    }

    @Override
    public void subTask(String subTaskName) {
        setTaskName(subTaskName);
    }

    @Override
    public void setTaskName(String name) {
        this.display.setInfo(name);
        this.info = name;
    }

    @Override
    public void done() {
        if (finishListener != null) {
            finishListener.finished();
        }
    }

    @Override
    public void beginTask(String taskName, String type) {
        this.display.setInfo(taskName);
        this.info = taskName;

    }

    @Override
    public void beginTask(String taskName, int progress) {
        setProgress(progress);
        this.display.setInfo(taskName);
        this.info = taskName;

        if (progress == IProgressMonitor.UNKNOWN) {
            display.setIndeterminate(true);
        }

    }

    @Override
    public void internalWorked(double work) {
        worked((int) work);
    }

    @Override
    public ISubMonitor convert() {
        return new SubProgressBar(this);
    }

    @Override
    public ISubMonitor convert(String title, int progress) {
        setProgress(progress);
        this.display.setInfo(title);
        this.info = title;

        return new SubProgressBar(this);
    }

    /**
     * @param finishListener FinishListener
     */
    public void setFinishListener(FinishListener finishListener) {
        this.finishListener = finishListener;
    }

    /**
     * Interface creates structure to listen progress bar events
     */
    public interface FinishListener {
        /**
         * Fires when progress monitor is finished
         */
        void finished();
    }

    /**
     * Creates a DisplayContainer that contains a progressBar and an
     * infoLabel. {#setProgress}, {#setInfo} and {#setIndeterminate} update
     * the elements UI-thread-safe with {#SwingUtilities.invokeLater}.
     */
    protected static class DisplayContainer {
        private JProgressBar progressBar;
        private JLabel infoLabel;

        /**
         * Creates a DisplayContainer that contains a progressBar and an
         * infoLabel.
         *
         * @param progressBar
         * @param infoLabel
         */
        public DisplayContainer(JProgressBar progressBar, JLabel infoLabel) {
            this.progressBar = progressBar;
            this.infoLabel = infoLabel;

            this.progressBar.setEnabled(true);
            this.progressBar.setMinimum(MIN_VALUE);
            this.progressBar.setMaximum(MAX_VALUE);
            this.progressBar.setVisible(true);

            if (infoLabel == null) {
                this.progressBar.setStringPainted(true);
            } else {
                this.infoLabel.setVisible(true);
            }
        }

        public JProgressBar getProgressBar() {
            return progressBar;
        }

        public JLabel getInfoLabel() {
            return infoLabel;
        }

        /**
         * Sets progress value with SwingUtilities.invokeLater
         *
         * @param progress
         */
        protected void setProgress(final int progress) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue(progress);
                }
            });
        }

        /**
         * Sets info to UI with SwingUtilities.invokeLater
         *
         * @param info additional progress information
         */
        protected void setInfo(final String info) {
            if (info == null) {
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (infoLabel == null) {
                        progressBar.setString(info);
                    } else {
                        infoLabel.setText(info);
                    }
                }
            });

        }

        protected void setIndeterminate(final boolean isIndeterminate) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setIndeterminate(isIndeterminate);
                }
            });
        }

        protected void reset() {
            setProgress(0);
            setInfo("");
        }
    }

}
