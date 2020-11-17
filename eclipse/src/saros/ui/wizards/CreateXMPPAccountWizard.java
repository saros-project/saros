package saros.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.wizards.pages.CreateXMPPAccountWizardPage;

/**
 * @JTourBusStop 4, The Interface Tour:
 *
 * <p>Another important element to the Saros interface is the Wizard. Eclipse supplies an abstract
 * Wizard class that can be extended with your concrete functionality.
 *
 * <p>In this example, the CreateXMPPAccountWizard allows the user to enter the details of a new
 * account, validate them and store them in our account store.
 */

/**
 * A wizard that is used to create XMPP accounts.
 *
 * <p><b>This Dialog is disabled due to missing Captcha Support! see {@link
 * #CREATE_DIALOG_ENABLED}</b>
 */
public class CreateXMPPAccountWizard extends Wizard {

  /**
   * This Registration logic just works for Servers without Captchas. Defacto it is broken as the
   * Saros server and all other public XMPP servers use Captchas. See issue #787 for more details.
   *
   * <p>TODO After Update to Smack 4 or higher add Captcha Support and enable this Dialog again.
   *
   * <p>see commit 37d4679a0673e536d304a69df9fc62e97d6c143a for removed code
   */
  public static final boolean CREATE_DIALOG_ENABLED = false;

  private final CreateXMPPAccountWizardPage createXMPPAccountPage;

  /*
   * Fields are cached in order to make the values accessible in case the
   * controls are already disposed. This is the case when the Wizard finished
   * or WizardDialog closed the Wizard.
   */
  protected String cachedServer;
  protected String cachedUsername;
  protected String cachedPassword;

  protected XMPPAccount createdXMPPAccount;

  public CreateXMPPAccountWizard(boolean showUseNowButton) {

    SarosPluginContext.initComponent(this);

    setWindowTitle(Messages.CreateXMPPAccountWizard_title);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_CREATE_XMPP_ACCOUNT));

    this.createXMPPAccountPage = new CreateXMPPAccountWizardPage(showUseNowButton);
    setNeedsProgressMonitor(true);
    setHelpAvailable(true);
  }

  @Override
  public void addPages() {
    addPage(createXMPPAccountPage);
  }

  /**
   * @JTourBusStop 5, The Interface Tour:
   *
   * <p>The performFinish() method is run when the user clicks the finish button on the wizard.
   */
  @Override
  public boolean performFinish() {
    return false;
  }

  /*
   * Wizard Results
   */

  /**
   * Returns the server (used) for account creation.
   *
   * @return
   */
  protected String getServer() {
    try {
      return createXMPPAccountPage.getServer();
    } catch (Exception e) {
      return cachedServer;
    }
  }

  /**
   * Returns the username (used) for account creation.
   *
   * @return
   */
  protected String getUsername() {
    try {
      return createXMPPAccountPage.getUsername();
    } catch (Exception e) {
      return cachedUsername;
    }
  }

  /**
   * Returns the password (used) for account creation.
   *
   * @return
   */
  protected String getPassword() {
    try {
      return createXMPPAccountPage.getPassword();
    } catch (Exception e) {
      return cachedPassword;
    }
  }

  /**
   * Returns the created {@link XMPPAccount}.
   *
   * @return null if the {@link XMPPAccount} has not (yet) been created.
   */
  public XMPPAccount getCreatedXMPPAccount() {
    return createdXMPPAccount;
  }
}
