package de.fu_berlin.inf.dpp.ui.util;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.ui.wizards.AddBuddyWizard;
import de.fu_berlin.inf.dpp.ui.wizards.AddXMPPAccountWizard;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.ui.wizards.CreateXMPPAccountWizard;
import de.fu_berlin.inf.dpp.ui.wizards.EditXMPPAccountWizard;
import de.fu_berlin.inf.dpp.ui.wizards.GettingStartedWizard;
import de.fu_berlin.inf.dpp.ui.wizards.ShareProjectAddBuddiesWizard;
import de.fu_berlin.inf.dpp.ui.wizards.ShareProjectAddProjectsWizard;
import de.fu_berlin.inf.dpp.ui.wizards.ShareProjectWizard;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.nebula.wizards.dialogs.CenteredWizardDialog;

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
     * @param parentShell
     * @param wizard
     * @param initialSize
     * @return
     */
    public static Integer openWizard(final Shell parentShell,
        final Wizard wizard, final Point initialSize) {
        try {
            return Utils.runSWTSync(new Callable<Integer>() {
                public Integer call() {
                    WizardDialog wizardDialog = new CenteredWizardDialog(
                        parentShell, wizard, initialSize);
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
    public static <W extends Wizard> W openWizardSuccessfully(
        final Shell parentShell, final W wizard, final Point initialSize) {
        Integer returnCode = openWizard(parentShell, wizard, initialSize);
        return (returnCode != null && returnCode == Window.OK) ? wizard : null;
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
        return openWizardSuccessfully(null, wizard, initialSize);
    }

    public static final Point GettingStartedWizardSize = new Point(950, 740);

    /**
     * Opens a {@link GettingStartedWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     * 
     * @param showConfigNote
     *            true if the last page should indicate that the Saros
     *            Configuration is going to open on finish
     * 
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static GettingStartedWizard openSarosGettingStartedWizard(
        final boolean showConfigNote) {

        try {
            return Utils.runSWTSync(new Callable<GettingStartedWizard>() {
                public GettingStartedWizard call() {

                    Shell shell = null;

                    try {
                        shell = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell();
                    } catch (Exception e) {
                        log.warn(
                            "Error while determining the main shell for the tutorial",
                            e);
                    }

                    int width = (int) (Display.getCurrent().getBounds().width * 0.66);
                    int height = (int) (Display.getCurrent().getBounds().height * 0.66);

                    if (width > GettingStartedWizardSize.x)
                        width = GettingStartedWizardSize.x;
                    if (height > GettingStartedWizardSize.y)
                        height = GettingStartedWizardSize.y;

                    return openWizardSuccessfully(shell,
                        new GettingStartedWizard(showConfigNote), new Point(
                            width, height));
                }
            });
        } catch (Exception e) {
            log.error("could not create getting started wizard", e);
            return null;
        }
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
     * Opens a {@link ConfigurationWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     * 
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static ConfigurationWizard openSarosConfigurationWizard() {
        return openWizardSuccessfully(new ConfigurationWizard(), new Point(850,
            440));
    }

    /**
     * Opens a {@link AddXMPPAccountWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     * 
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static AddXMPPAccountWizard openAddXMPPAccountWizard() {
        return openWizardSuccessfully(new AddXMPPAccountWizard(), new Point(
            850, 500));
    }

    /**
     * Opens a {@link CreateXMPPAccountWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     * 
     * @param showUseNowButton
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static CreateXMPPAccountWizard openCreateXMPPAccountWizard(
        boolean showUseNowButton) {
        return openWizardSuccessfully(new CreateXMPPAccountWizard(
            showUseNowButton), new Point(530, 580));
    }

    /**
     * Opens a {@link EditXMPPAccountWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     * 
     * @param account
     *            to be edited; null if the current account should be edited
     *            (creates one if no active account is set)
     * @return the wizard if it was successfully finished; null otherwise
     */
    public static EditXMPPAccountWizard openEditXMPPAccountWizard(
        XMPPAccount account) {
        return openWizardSuccessfully(new EditXMPPAccountWizard(account),
            new Point(500, 400));
    }

    /**
     * Opens a {@link AddBuddyWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     */
    public static AddBuddyWizard openAddBuddyWizard() {
        return openWizardSuccessfully(new AddBuddyWizard(), new Point(500, 320));
    }

    /**
     * Opens a {@link ShareProjectWizard} in the SWT thread and returns the
     * displayed instance in case of success.
     */
    public static ShareProjectWizard openShareProjectWizard() {
        return openWizardSuccessfully(new ShareProjectWizard(), new Point(750,
            550));
    }

    /**
     * Opens a {@link ShareProjectAddProjectsWizard} in the SWT thread and
     * returns the displayed instance in case of success.
     */
    public static ShareProjectAddProjectsWizard openShareProjectAddProjectsWizard() {
        return openWizardSuccessfully(new ShareProjectAddProjectsWizard(),
            new Point(750, 550));
    }

    /**
     * Opens a {@link ShareProjectAddBuddiesWizard} in the SWT thread and
     * returns the displayed instance in case of success.
     */
    public static ShareProjectAddBuddiesWizard openShareProjectAddBuddiesWizard() {
        return openWizardSuccessfully(new ShareProjectAddBuddiesWizard(),
            new Point(750, 550));
    }
}
