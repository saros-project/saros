/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.PreferenceUtils;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * A wizard that guides the user through an incoming invitation process.
 * 
 * TODO Automatically switch to follow mode
 * 
 * TODO Suggest if the project is a CVS project that the user checks it out and
 * offers an option to transfer the settings
 * 
 * TODO Create a separate Wizard class with the following concerns implemented
 * more nicely: Long-Running Operation after each step, cancelation by a remote
 * party, auto-advance.
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {

    private static final Logger log = Logger.getLogger(JoinSessionWizard.class
        .getName());

    protected ShowDescriptionPage descriptionPage;

    protected EnterProjectNamePage namePage;

    protected WizardDialogAccessable wizardDialog;

    protected IIncomingInvitationProcess process;

    protected boolean requested = false;

    protected String updateProjectName;

    protected boolean updateSelected;

    protected DataTransferManager dataTransferManager;

    protected PreferenceUtils preferenceUtils;

    public JoinSessionWizard(IIncomingInvitationProcess process,
        DataTransferManager dataTransferManager, PreferenceUtils preferenceUtils) {
        this.process = process;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;

        setWindowTitle("Session Invitation");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
    }

    public PreferenceUtils getPreferenceUtils() {
        return preferenceUtils;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {

        if (page.equals(descriptionPage) && !requested) {
            /*
             * Increment pages for auto-next, because the request will block too
             * long
             */
            pageChanges++;
            if (!requestHostFileList()) {
                return null;
            }
        }
        return super.getNextPage(page);
    }

    public boolean requestHostFileList() {
        requested = true;

        /* wait for getting project file list. */
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InterruptedException {
                    process.requestRemoteFileList(monitor);
                }
            });
        } catch (InvocationTargetException e) {
            log.warn("Exception while requesting remote file list", e);
        } catch (InterruptedException e) {
            log.debug("Request of remote file list canceled/interrupted", e);
            // User canceled...
            getShell().close();
            return false;
        }

        if (process.getRemoteFileList() == null) {
            // Remote side canceled...
            getShell().close();
            return false;
        }

        if (preferenceUtils.isAutoReuseExisting()
            && JoinSessionWizardUtils.existsProjects(process.getProjectName())) {
            updateSelected = true;
            updateProjectName = process.getProjectName();
        } else {
            updateSelected = false;
            updateProjectName = "";
        }

        if (process.getState() == State.CANCELED) {
            return false;
        }
        return true;
    }

    public void showCancelMessage(JID jid, String errorMsg, boolean replicated) {

        if (errorMsg != null) {
            MessageDialog.openError(getShell(), "Invitation aborted",
                "Could not complete invitation with " + jid.getBase()
                    + " because an error occurred:\n\n" + errorMsg);
        } else {
            // errorMsg == null means canceled either by us or peer
            if (replicated) {
                MessageDialog.openInformation(getShell(),
                    "Invitation cancelled",
                    "Invitation was cancelled by inviter (" + jid.getBase()
                        + ").");
            }
        }

        if (replicated) {
            /*
             * TODO The entanglement between UI and process is too complicated
             * to sort out, how to close this dialog when it is currently
             * executing the finishing synchronization
             */
            wizardDialog.close();
        }

    }

    IInvitationUI ui = new IInvitationUI() {
        public void cancel(final JID jid, final String errorMsg,
            final boolean replicated) {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    showCancelMessage(jid, errorMsg, replicated);
                }
            });
        }

        public void runGUIAsynch(Runnable runnable) {
            // TODO this cannot be ignored an InvitationUI like the
            // JoinSessionWizard need to implement this
            assert false;
        }

        public void updateInvitationProgress(JID jid) {
            // ignored, not needed atm
        }
    };

    public IInvitationUI getInvitationUI() {
        return ui;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);
        // create namePage lazily
    }

    @Override
    public void addPages() {
        this.descriptionPage = new ShowDescriptionPage(this);
        this.namePage = new EnterProjectNamePage(this, dataTransferManager,
            preferenceUtils);

        addPage(this.descriptionPage);
        addPage(this.namePage);
    }

    @Override
    public boolean performFinish() {

        if (this.process.getState() == State.CANCELED) {
            return false;
        }

        final IProject source = this.namePage.getSourceProject();
        final String target = this.namePage.getTargetProjectName();

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {

                    JoinSessionWizard.this.process.accept(source, target,
                        monitor);
                }
            });
        } catch (InvocationTargetException e) {
            log.warn("Exception while requesting remote file list", e);
            return false;
        } catch (InterruptedException e) {
            log.debug("Request of remote file list canceled/interrupted", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean performCancel() {
        try {
            this.process.cancel(null, false);
        } catch (RuntimeException e) {
            log.error("Failed to cancel process: ", e);
        }

        return true;
    }

    public void setWizardDlg(WizardDialogAccessable wd) {
        this.wizardDialog = wd;

        /**
         * Listen to page changes so we can cancel our automatic clicking the
         * next button
         */
        this.wizardDialog.addPageChangingListener(new IPageChangingListener() {
            public void handlePageChanging(PageChangingEvent event) {
                pageChanges++;
            }
        });

    }

    /**
     * Variable is only used to count how many times pageChanges were registered
     * so we only press the next button if no page changes occurred.
     * 
     * The only place this is needed is below in the pressWizardButton, if you
     * want to know the number of page changes, count them yourself.
     */
    private int pageChanges = 0;

    protected boolean disposed = false;

    /**
     * Will wait one second and then press the next button, if the dialog is
     * still on the given page (i.e. if the user presses next then this will not
     * call next again).
     */
    public void pressWizardButton(final int buttonID) {

        final int pageChangesAtStart = pageChanges;

        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("Internal error: ", e);
                    return;
                }
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        if (pageChangesAtStart == pageChanges && !disposed)
                            wizardDialog.buttonPressed(buttonID);
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {
        disposed = true;
        super.dispose();
    }

    public String getUpdateProject() {
        return updateProjectName;
    }

    public boolean isUpdateSelected() {
        return updateSelected;
    }
}
