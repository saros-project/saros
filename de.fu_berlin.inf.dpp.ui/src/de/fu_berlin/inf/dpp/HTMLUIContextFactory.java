package de.fu_berlin.inf.dpp;

import static de.fu_berlin.inf.dpp.context.AbstractContextFactory.Component.create;

import de.fu_berlin.inf.dpp.context.AbstractContextFactory;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseAccountWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.ConnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.DisconnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.EditAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.RenameContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.SaveAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation;
import de.fu_berlin.inf.dpp.ui.browser_functions.SetActiveAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowAccountPage;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowSessionWizard;
import de.fu_berlin.inf.dpp.ui.core_facades.ConnectionFacade;
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade;
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.pages.AccountPage;
import de.fu_berlin.inf.dpp.ui.pages.ConfigurationPage;
import de.fu_berlin.inf.dpp.ui.pages.MainPage;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;
import org.picocontainer.MutablePicoContainer;

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
