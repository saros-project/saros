/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;

/**
 * A wizard that guides the user through an incoming invitiation process.
 * 
 * Todo:
 *   - Enhance Usability of this dialog:
 *     - Automatically switch to follow mode
 *     - Make a suggestion for the name of the project
 *   - Suggest if the project is a CVS project that the user checks 
 *     it out and offers an option to transfer the settings
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {
    private static Logger log = Logger.getLogger(
        JoinSessionWizard.class.getName());
    
    private ShowDescriptionPage              descriptionPage;
    private EnterNamePage                    namePage;

    private final IIncomingInvitationProcess process;

    /**
     * A wizard page that displays the name of the inviter and the description
     * provided with the invitation.
     */
    private class ShowDescriptionPage extends WizardPage {
        protected ShowDescriptionPage() {
            super("firstPage");
            
            setTitle("Session Invitation");
            setDescription("You have been invited to join on a session for a " +
                "shared project. Click next if you want to accept the invitation.");
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IDialogPage
         */
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            
            Label inviterLabel = new Label(composite, SWT.NONE);
            inviterLabel.setText("Inviter");
            
            Text inviterText = new Text(composite, 
                SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
            
            inviterText.setLayoutData(new GridData(
                SWT.FILL, SWT.CENTER, false, false));
            inviterText.setText(process.getPeer().getBase());
            
            Label descriptionLabel = new Label(composite, SWT.NONE);
            descriptionLabel.setLayoutData(new GridData(
                SWT.FILL, SWT.BEGINNING, false, false));
            descriptionLabel.setText("Project");
            
            Text descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
            descriptionText.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL, true, true));
            descriptionText.setText(process.getDescription());
            
            setControl(composite);
        }
    }
    
    /**
     * A wizard page that allows to enter the new project name or to choose to
     * overwrite the project selected by the {@link ProjectSelectionPage}.
     */
    private class EnterNamePage extends WizardPage {
        private Text   newProjectNameText;

        protected EnterNamePage() {
            super("namePage");
            
            setTitle("Session Invitation");
            setDescription("Enter the name of the new project.");
            
            updatePageComplete();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.IDialogPage
         */
        public void createControl(Composite parent) {
            requestRemoteFileList();
            
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            
            Label helpLabel = new Label(composite, SWT.WRAP);
            helpLabel.setText(getHelpText());
            helpLabel.setLayoutData(new GridData(
                SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
            
            Label newProjectNameLabel = new Label(composite, SWT.NONE);
            newProjectNameLabel.setText("Project name");
            GridData gridData = new GridData(
                SWT.FILL, SWT.BEGINNING, false, false);
            gridData.verticalIndent = 20;
            newProjectNameLabel.setLayoutData(gridData);
            
            newProjectNameText = new Text(composite, SWT.BORDER);
            gridData = new GridData(
                SWT.FILL, SWT.BEGINNING, true, false);
            gridData.verticalIndent = 20;
            newProjectNameText.setLayoutData(gridData);
            newProjectNameText.setFocus();
            
            attachListeners();
            setControl(composite);
        }
        
        /**
         * @return the project name of the project that should be created or
         * <code>null</code> if the user chose to overwrite an existing
         * project.
         */
        public String getNewProjectName() {
            return newProjectNameText != null ? newProjectNameText.getText() : "";
        }
        
        private IProject getLocalProject() {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject[] projects = workspace.getRoot().getProjects();
            
            int maxMatch = 0;
            IProject selectedProject = null;
            for (int i = 0; i < projects.length; i++) {
                if (!projects[i].isOpen())
                    continue;
                
                int match = getMatch(projects[i]);
                
                if (match > maxMatch) {
                    maxMatch = match;
                    selectedProject = projects[i];
                }
            }
            
            return selectedProject;
        }
        
        private int getMatch(IProject project) {
            try {
                FileList remoteFileList = process.getRemoteFileList();
                return remoteFileList.match(new FileList(project));
            } catch (CoreException e) {
                log.log(Level.FINE, "Couldn't calculate match for project "+project, e);
                
                return 0;
            }
        }

        private void requestRemoteFileList() {
            try {
                getContainer().run(true, false, new IRunnableWithProgress(){
                    public void run(IProgressMonitor monitor) {
                        
                        process.requestRemoteFileList(monitor);
                    }
                });
                
            } catch (InvocationTargetException e) {
                log.log(Level.WARNING, "Exception while requesting remote file list", e);
                
            } catch (InterruptedException e) {
                log.log(Level.FINE, "Request of remote file list canceled/interrupted", e);
            }
        }

        private String getHelpText() {
            IProject project = getLocalProject();
            if (project == null) {
                return "Project replication will start from scratch.";
            }
            
            return "It has been detected that one of your local projects (" 
                + project.getName()
                + ") has an identicallness of " + getMatch(project) + "%.\n"
                + "This fact will used to shorten the process of "
                + "replicating the remote project.";
        }

        private void attachListeners() {
            newProjectNameText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });
        }
        
        private void updatePageComplete() {
            setPageComplete(getNewProjectName().length() >= 1);
        }
    }
    
    public JoinSessionWizard(IIncomingInvitationProcess process) {
        this.process = process;
        
        setWindowTitle("Session Invitation");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
    }
    
    @Override
    public void addPages() {
        descriptionPage = new ShowDescriptionPage();
        namePage = new EnterNamePage();

        addPage(descriptionPage);
        addPage(namePage);
    }
    
    @Override
    public void createPageControls(Composite pageContainer) {
        descriptionPage.createControl(pageContainer);
        // create namePage lazily
    }
    
    @Override
    public boolean performFinish() {
        final IProject project = namePage.getLocalProject();
        final String newProjectName = namePage.getNewProjectName();
        
        try {
            getContainer().run(true, true, new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) 
                    throws InvocationTargetException, InterruptedException {
                    
                    process.accept(project, newProjectName, monitor);
                }
            });
        } catch (InvocationTargetException e) {
            log.log(Level.WARNING, "Exception while requesting remote file list", e);
            
        } catch (InterruptedException e) {
            log.log(Level.FINE, "Request of remote file list canceled/interrupted", e);
        }
        
        return true;
    }
    
    @Override
    public boolean performCancel() {
        process.cancel(null, false);
        
        return super.performCancel();
    }
}
