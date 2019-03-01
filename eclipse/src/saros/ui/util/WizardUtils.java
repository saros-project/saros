package saros.ui.util;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import saros.account.XMPPAccount;
import saros.ui.wizards.AddContactWizard;
import saros.ui.wizards.AddContactsToSessionWizard;
import saros.ui.wizards.AddResourcesToSessionWizard;
import saros.ui.wizards.AddXMPPAccountWizard;
import saros.ui.wizards.ConfigurationWizard;
import saros.ui.wizards.CreateXMPPAccountWizard;
import saros.ui.wizards.EditXMPPAccountWizard;
import saros.ui.wizards.StartSessionWizard;
import saros.ui.wizards.dialogs.ConfigurationWizardDialog;

/** Utility class for {@link IWizard}s */
public class WizardUtils {
  private static final Logger log = Logger.getLogger(WizardUtils.class.getName());

  /**
   * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s return code.
   *
   * @param parentShell
   * @param wizard
   * @return
   */
  public static Integer openWizard(final Shell parentShell, final Wizard wizard) {
    try {
      return SWTUtils.runSWTSync(
          new Callable<Integer>() {
            @Override
            public Integer call() {
              WizardDialog wizardDialog = new WizardDialog(parentShell, wizard);
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
   * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s reference to the {@link
   * Wizard} in case of success.
   *
   * @param wizard
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static <W extends Wizard> W openWizardSuccessfully(
      final Shell parentShell, final W wizard) {
    Integer returnCode = openWizard(parentShell, wizard);
    return (returnCode != null && returnCode == Window.OK) ? wizard : null;
  }

  /**
   * Open a wizard in the SWT thread and returns the {@link WizardDialog}'s reference to the {@link
   * Wizard} in case of success.
   *
   * @param wizard
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static <W extends Wizard> W openWizardSuccessfully(final W wizard) {
    return openWizardSuccessfully(null, wizard);
  }

  /**
   * Runs the {@link NewProjectAction} in the SWT thread in order to create a new project wizard.
   */
  public static void openNewProjectWizard() {
    SWTUtils.runSafeSWTSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            NewProjectAction newProjectAction = new NewProjectAction(window);
            newProjectAction.run();
          }
        });
  }

  /**
   * Opens a {@link ConfigurationWizard} in the SWT thread and returns the displayed instance in
   * case of success.
   *
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static ConfigurationWizard openSarosConfigurationWizard() {
    final ConfigurationWizard wizard = new ConfigurationWizard();

    /*
     * must open the wizard with a ConfigurationWizardDialog because of an
     * extra Create Account button in the WizardDialog button bar
     */
    try {
      int code =
          SWTUtils.runSWTSync(
              new Callable<Integer>() {
                @Override
                public Integer call() {
                  WizardDialog wizardDialog = new ConfigurationWizardDialog(null, wizard);
                  wizardDialog.setHelpAvailable(false);
                  return wizardDialog.open();
                }
              });

      if (code != Window.OK) return null;

    } catch (Exception e) {
      log.warn("Error opening wizard " + wizard.getWindowTitle(), e);
      return null;
    }

    return wizard;
  }

  /**
   * Opens a {@link AddXMPPAccountWizard} in the SWT thread and returns the displayed instance in
   * case of success.
   *
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static AddXMPPAccountWizard openAddXMPPAccountWizard() {
    return openWizardSuccessfully(new AddXMPPAccountWizard());
  }

  /**
   * Opens a {@link CreateXMPPAccountWizard} in the SWT thread and returns the displayed instance in
   * case of success.
   *
   * @param showUseNowButton
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static CreateXMPPAccountWizard openCreateXMPPAccountWizard(boolean showUseNowButton) {
    return openWizardSuccessfully(new CreateXMPPAccountWizard(showUseNowButton));
  }

  /**
   * Opens a {@link EditXMPPAccountWizard} in the SWT thread and returns the displayed instance in
   * case of success.
   *
   * @param account to be edited; null if the current account should be edited (creates one if no
   *     active account is set)
   * @return the wizard if it was successfully finished; null otherwise
   */
  public static EditXMPPAccountWizard openEditXMPPAccountWizard(XMPPAccount account) {
    return openWizardSuccessfully(new EditXMPPAccountWizard(account));
  }

  /**
   * Opens a {@link AddContactWizard} in the SWT thread and returns the displayed instance in case
   * of success.
   */
  public static AddContactWizard openAddContactWizard() {
    return openWizardSuccessfully(new AddContactWizard());
  }

  /**
   * Opens a {@link StartSessionWizard} in the SWT thread and returns the displayed instance in case
   * of success.
   *
   * @param preselectedResources resources that should be preselected or <code>null</code>
   */
  public static StartSessionWizard openStartSessionWizard(
      final Collection<IResource> preselectedResources) {
    return openWizardSuccessfully(new StartSessionWizard(preselectedResources));
  }

  /**
   * Opens a {@link AddResourcesToSessionWizard} in the SWT thread and returns the displayed
   * instance in case of success.
   *
   * @param preselectedResources resources that should be preselected or <code>null</code>
   */
  public static AddResourcesToSessionWizard openAddResourcesToSessionWizard(
      final Collection<IResource> preselectedResources) {
    return openWizardSuccessfully(new AddResourcesToSessionWizard(preselectedResources));
  }

  /**
   * Opens a {@link AddContactsToSessionWizard} in the SWT thread and returns the displayed instance
   * in case of success.
   */
  public static AddContactsToSessionWizard openAddContactsToSessionWizard() {
    return openWizardSuccessfully(new AddContactsToSessionWizard());
  }
}
