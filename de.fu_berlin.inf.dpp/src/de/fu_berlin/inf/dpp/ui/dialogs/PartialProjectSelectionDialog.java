package de.fu_berlin.inf.dpp.ui.dialogs;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This Dialog is used to allow the host to share only part of a project
 */
public class PartialProjectSelectionDialog {

    protected IProject project;

    protected Shell parentShell;

    protected PartialProjectWizard wizard;

    protected SarosResourceSelectionPage page;

    protected WizardDialog dialog;

    public PartialProjectSelectionDialog(Shell parentShell, IProject project) {
        this.project = project;
        this.parentShell = parentShell;

        wizard = new PartialProjectWizard();
        wizard.setWindowTitle("Saros - Configure project to share");
        page = new SarosResourceSelectionPage("Select Resources to Share",
            new StructuredSelection(project));
        wizard.addPage(page);

        dialog = new WizardDialog(parentShell, wizard);
    }

    public static class PartialProjectWizard extends Wizard {

        @Override
        public boolean performFinish() {
            return true;
        }
    }

    public class SarosResourceSelectionPage extends WizardExportResourcesPage {

        protected SarosResourceSelectionPage(String pageName,
            IStructuredSelection selection) {
            super(pageName, selection);
            setTitle("Select Files to Share");
            setDescription("Please select all files which should "
                + "be shared using Saros initially.\n"
                + "Removing big files will improve the"
                + " speed of the initial synchronisation.");
        }

        public void handleEvent(Event event) {
            // Do nothing

        }

        @SuppressWarnings("unchecked")
        @Override
        public List<IResource> getSelectedResources() {
            return super.getSelectedResources();
        }

        @Override
        protected void createDestinationGroup(Composite parent) {
            // We do not need any UI right now
        }

        @Override
        protected void createOptionsGroup(Composite parent) {
            // We do not need any UI right now
        }

        @SuppressWarnings("unchecked")
        @Override
        protected boolean validateSourceGroup() {

            for (IResource resource : Utils
                .asIterable((Iterator<IResource>) getSelectedResourcesIterator())) {
                if (!project.equals(resource.getProject())) {
                    setErrorMessage("Currently only files from the main project can be shared");
                    return false;
                }
            }
            return true;

        }

    }

    public List<IResource> getSelectedResources() {
        return page.getSelectedResources();

    }

    public int open() {
        return dialog.open();
    }
}
