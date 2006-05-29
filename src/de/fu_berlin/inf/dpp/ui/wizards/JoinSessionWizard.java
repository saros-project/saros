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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.internal.InvitationProcess.InvitationException;

public class JoinSessionWizard extends Wizard {
    private FirstPage                        firstPage;
    private ProjectSelectionPage             projectSelectionPage;
    
    private ListViewer                       projectList;
    private final IIncomingInvitationProcess process;

    private class FirstPage extends WizardPage {
        protected FirstPage() {
            super("firstPage");
            
            setTitle("Session Invitation");
            setDescription("You have been invited to join on a session for a shared " +
                "project. Click next if you want to accept the invitation.");
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            
            Label inviterLabel = new Label(composite, SWT.NONE);
            inviterLabel.setText("Inviter");
            
            Text inviterText = new Text(composite, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
            inviterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            inviterText.setText(process.getPeer().getJID());
            
            Label descriptionLabel = new Label(composite, SWT.NONE);
            descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
            descriptionLabel.setText("Description");
            
            Text descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
            descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            descriptionText.setText(process.getDescription());
            
            setControl(composite);
        }
    }
    
    private class ProjectSelectionPage extends WizardPage {
        private Text   newProjectNameText;
        private Button createNewProjectButton;
        private Button useExistingProjectButton;

        protected ProjectSelectionPage() {
            super("selectionPage");
            
            setTitle("Session Invitation");
            setDescription("Before joining the shared project it needs to be fully replicated " +
                "on your system.");
            
            setPageComplete(false);
        }

        public void createControl(Composite parent) {
            requestRemoteFileList();
            
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            
            new Label(composite, SWT.NONE); // empty
            
            Label helpLabel = new Label(composite, SWT.WRAP);
            helpLabel.setText("To shorten the replication of the project, you can use one of your " +
                "local projects as the base project. The number in paranthesis show the identicalness " +
                "of that project, with the remotely shared project.");
            GridData gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
            gridData.widthHint = 600;
            helpLabel.setLayoutData(gridData);
            
            Label projectListLabel = new Label(composite, SWT.NONE);
            projectListLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
            projectListLabel.setText("Local project");
            
            createProjectList(composite);
            projectList.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            projectList.setInput(projects);
            
            gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1);
            gridData.verticalIndent = 25;
            Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
            separator.setLayoutData(gridData);
            
            createNewProjectButton = new Button(composite, SWT.RADIO);
            createNewProjectButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
            createNewProjectButton.setText("Create in new project");
            createNewProjectButton.setSelection(true);
            
            Label newProjectNameLabel = new Label(composite, SWT.NONE);
            gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
            gridData.horizontalIndent = 20;
            newProjectNameLabel.setText("Project name");
            newProjectNameLabel.setLayoutData(gridData);
            
            newProjectNameText = new Text(composite, SWT.BORDER);
            newProjectNameText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
            
            useExistingProjectButton = new Button(composite, SWT.RADIO);
            useExistingProjectButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
            useExistingProjectButton.setText("Overwrite project");
            
            attachListeners();
            setControl(composite);
        }
        
        /**
         * @return the project name of the project that should be created or
         * <code>null</code> if the user chose to overwrite an existing
         * project.
         */
        public String getNewProjectName() {
            return createNewProjectButton.getSelection() ? newProjectNameText.getText() : null;
        }
        
        private void requestRemoteFileList() {
            try {
                getContainer().run(true, false, new IRunnableWithProgress(){
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, 
                        InterruptedException {
                        
                        process.requestRemoteFileList(monitor);
                    }
                });
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        private void attachListeners() {
            SelectionListener selectionListener = new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    newProjectNameText.setEditable(createNewProjectButton.getSelection());
                    updatePageComplete();
                }
        
                public void widgetDefaultSelected(SelectionEvent e) {
                    // ignore
                }
            };
            
            createNewProjectButton.addSelectionListener(selectionListener);
            useExistingProjectButton.addSelectionListener(selectionListener);
            
            newProjectNameText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updatePageComplete();
                }
            });
        }
        
        private void createProjectList(Composite composite) {
            projectList = new ListViewer(composite);
            projectList.setContentProvider(new IStructuredContentProvider(){
                private IProject[] projects;

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    projects = (IProject[])newInput;
                }

                public Object[] getElements(Object inputElement) {
                    return projects;
                }
                
                public void dispose() {
                }
            });
            
            projectList.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    IProject project = (IProject)element;
                    
                    //HACK
                    int match = -1;
                    try {
                        // UGLYHACK! CAN RETURN NULL!
                        match = process.getRemoteFileList().match(new FileList(project));
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    if (match >= 0) {
                        return project.getName() + " (" + match + "%)";
                    } else {
                        return project.getName();
                    }
                }
            });
            
            projectList.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    updatePageComplete();
                }
            });
        }
        
        private void updatePageComplete() {
            IStructuredSelection selection = (IStructuredSelection)projectList.getSelection();
            
            setPageComplete(selection.size() == 1 && 
                (getNewProjectName() == null || getNewProjectName().length() > 0));
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
        firstPage = new FirstPage();
        projectSelectionPage = new ProjectSelectionPage();

        addPage(firstPage);
        addPage(projectSelectionPage);
    }
    
    @Override
    public void createPageControls(Composite pageContainer) {
        firstPage.createControl(pageContainer);
        // create projectSelectionPage lazily
    }
    
    @Override
    public boolean performFinish() {
        IStructuredSelection selection = (IStructuredSelection)projectList.getSelection();
        final IProject project = (IProject)selection.getFirstElement();
        final String newProjectName = projectSelectionPage.getNewProjectName();
        
        try {
            getContainer().run(true, true, new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) throws InvocationTargetException, 
                    InterruptedException {
                    
                    try {
                        process.accept(project, newProjectName, monitor);
                    } catch (InvitationException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return true;
    }
}
