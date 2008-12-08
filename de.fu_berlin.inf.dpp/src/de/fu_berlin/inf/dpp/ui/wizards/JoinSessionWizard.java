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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A wizard that guides the user through an incoming invitiation process.
 * 
 * Todo:
 * 
 * o Automatically switch to follow mode
 * 
 * o Suggest if the project is a CVS project that the user checks it out and
 * offers an option to transfer the settings
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {

    static Logger log = Logger.getLogger(JoinSessionWizard.class.getName());

    ShowDescriptionPage descriptionPage;

    EnterNamePage namePage;

    WizardDialogAccessable myWizardDlg;

    IIncomingInvitationProcess process;

    Display current;

    public JoinSessionWizard(IIncomingInvitationProcess process) {
        this.process = process;

        this.current = Display.getCurrent();

        setWindowTitle("Session Invitation");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
    }

    public IInvitationUI getInvitationUI() {
        return new IInvitationUI() {

            public void cancel(final String errorMsg, final boolean replicated) {
                JoinSessionWizard.this.current.asyncExec(new Runnable() {
                    public void run() {

                        if (errorMsg != null) {
                            MessageDialog.openError(getShell(),
                                    "Invitation aborted",
                                    "Could not complete invitation because an error occurred ("
                                            + errorMsg + ")");
                        } else {
                            // errorMsg == null means canceled either by us or
                            // peer
                            if (replicated) {
                                MessageDialog.openInformation(getShell(),
                                        "Invitation cancelled",
                                        "Invitation was cancelled by peer.");
                            }
                        }
                        JoinSessionWizard.this.myWizardDlg.close();
                    }
                });
            }

            public void runGUIAsynch(Runnable runnable) {
                // ignored, not needed atm
            }

            public void updateInvitationProgress(JID jid) {
                // ignored, not needed atm
            }
        };
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);
        // create namePage lazily
    }

    @Override
    public void addPages() {
        this.descriptionPage = new ShowDescriptionPage(this);
        this.namePage = new EnterNamePage(this);

        addPage(this.descriptionPage);
        addPage(this.namePage);
    }

    @Override
    public boolean performFinish() {

        if (this.process.getState() == State.CANCELED) {
            return true;
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
            JoinSessionWizard.log.log(Level.WARNING,
                    "Exception while requesting remote file list", e);
        } catch (InterruptedException e) {
            JoinSessionWizard.log.log(Level.FINE,
                    "Request of remote file list canceled/interrupted", e);
        }

        return true;
    }

    @Override
    public boolean performCancel() {
        this.process.cancel(null, false);

        return super.performCancel();
    }

    public void setWizardDlg(WizardDialogAccessable wd) {
        this.myWizardDlg = wd;
    }
}
