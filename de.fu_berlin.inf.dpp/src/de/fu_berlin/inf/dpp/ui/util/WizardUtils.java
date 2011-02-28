package de.fu_berlin.inf.dpp.ui.util;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;

import de.fu_berlin.inf.dpp.ui.wizards.AddBuddyWizard;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.CenteredWizardDialog;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Utility class for {@link IWizard}s
 */
public class WizardUtils {
    private static final Logger log = Logger.getLogger(WizardUtils.class
        .getName());

    /**
     * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s
     * return code.
     * 
     * @param wizard
     * @param initialSize
     * @return
     */
    public static Integer openWizard(final Wizard wizard,
        final Point initialSize) {
        try {
            return Utils.runSWTSync(new Callable<Integer>() {
                public Integer call() {
                    WizardDialog wizardDialog = new CenteredWizardDialog(null,
                        wizard, initialSize);
                    wizardDialog.setHelpAvailable(false);
                    return wizardDialog.open();
                }
            });
        } catch (Exception e) {
            log.warn("Error opening wizard " + wizard.getWindowTitle(), e);
        }
        return null;
    }

    /**
     * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s
     * reference to the {@link Wizard} in case of success.
     * 
     * @param wizard
     * @param initialSize
     * 
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static <W extends Wizard> W openWizardSuccessfully(final W wizard,
        final Point initialSize) {
        Integer returnCode = openWizard(wizard, initialSize);
        return (returnCode != null && returnCode == Window.OK) ? wizard : null;
    }

    /**
     * Runs the {@link NewProjectAction} in the SWT thread in order to create a
     * new project wizard.
     */
    public static void openNewProjectWizard() {
        Utils.runSafeSWTSync(log, new Runnable() {
            public void run() {
                IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
                NewProjectAction newProjectAction = new NewProjectAction(window);
                newProjectAction.run();
            }
        });
    }

    /**
     * Opens a {@link AddBuddyWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     */
    public static AddBuddyWizard openAddBuddyWizard() {
        return openWizardSuccessfully(new AddBuddyWizard(), new Point(500, 320));
    }
}
