package saros;

import static saros.context.AbstractContextFactory.Component.create;

import org.picocontainer.MutablePicoContainer;
import saros.context.AbstractContextFactory;
import saros.ui.browser_functions.AddContact;
import saros.ui.browser_functions.CloseAccountWizard;
import saros.ui.browser_functions.CloseSessionInvitationWizard;
import saros.ui.browser_functions.ConnectAccount;
import saros.ui.browser_functions.DeleteAccount;
import saros.ui.browser_functions.DeleteContact;
import saros.ui.browser_functions.DisconnectAccount;
import saros.ui.browser_functions.EditAccount;
import saros.ui.browser_functions.GetValidJID;
import saros.ui.browser_functions.RenameContact;
import saros.ui.browser_functions.SaveAccount;
import saros.ui.browser_functions.SendInvitation;
import saros.ui.browser_functions.SetActiveAccount;
import saros.ui.browser_functions.ShowAccountPage;
import saros.ui.browser_functions.ShowSessionWizard;
import saros.ui.core_facades.ConnectionFacade;
import saros.ui.core_facades.RosterFacade;
import saros.ui.ide_embedding.BrowserCreator;
import saros.ui.manager.BrowserManager;
import saros.ui.manager.ProjectListManager;
import saros.ui.pages.AccountPage;
import saros.ui.pages.ConfigurationPage;
import saros.ui.pages.MainPage;
import saros.ui.pages.SessionWizardPage;
import saros.ui.renderer.AccountRenderer;
import saros.ui.renderer.ProjectListRenderer;
import saros.ui.renderer.StateRenderer;

/**
 * This is the HTML UI core factory for Saros. All components that are created by this factory
 * <b>must</b> be working on any platform the application is running on. @JTourBusStop 4, Extending
 * the HTML GUI, PicoContainer components:
 *
 * <p>If you created a new class in the ui module that should be initialised by the PicoContainer,
 * you have to add it here.
 */
public class HTMLUIContextFactory extends AbstractContextFactory {

  private MutablePicoContainer container;

  @Override
  public void createComponents(MutablePicoContainer container) {
    this.container = container;
    createBrowserfunctions();
    createPages();
    createRenderer();
    createFacades();
    createMisc();
  }

  private void createBrowserfunctions() {
    // please use alphabetic order
    add(
        AddContact.class, //
        CloseAccountWizard.class, //
        CloseSessionInvitationWizard.class, //
        ConnectAccount.class, //
        DeleteAccount.class, //
        DeleteContact.class, //
        DisconnectAccount.class, //
        EditAccount.class, //
        GetValidJID.class, //
        RenameContact.class, //
        SaveAccount.class, //
        SendInvitation.class, //
        SetActiveAccount.class, //
        ShowAccountPage.class, //
        ShowSessionWizard.class);
  }

  private void createPages() {
    add(AccountPage.class, MainPage.class, SessionWizardPage.class, ConfigurationPage.class);
  }

  private void createRenderer() {
    add(AccountRenderer.class, StateRenderer.class, ProjectListRenderer.class);
  }

  private void createFacades() {
    add(ConnectionFacade.class, RosterFacade.class);
  }

  /** For UI components that fits no where else. */
  private void createMisc() {
    // TODO: Dodgy naming
    add(BrowserCreator.class, BrowserManager.class, ProjectListManager.class);
  }

  /**
   * Add the components to the container
   *
   * @param classes to add
   */
  private void add(Class<?>... classes) {
    for (Class<?> clazz : classes) {
      Component component = create(clazz);
      container.addComponent(component.getBindKey(), component.getImplementation());
    }
  }
}
