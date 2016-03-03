package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.ui.browser_functions.AddContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseAccountWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.ConnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.DisconnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.RenameContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.SaveAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowAccountPage;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowSessionWizard;
import de.fu_berlin.inf.dpp.ui.core_facades.AccountStoreFacade;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.manager.ProjectListManager;
import de.fu_berlin.inf.dpp.ui.pages.AccountPage;
import de.fu_berlin.inf.dpp.ui.pages.MainPage;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * This is the HTML UI core factory for Saros. All components that are created
 * by this factory <b>must</b> be working on any platform the application is
 * running on.
 * 
 * @JTourBusStop 4, Extending the HTML GUI, PicoContainer components:
 * 
 *               If you created a new class in the ui module that should be
 *               initialised by the PicoContainer, you have to add it here.
 */
public class HTMLUIContextFactory extends AbstractSarosContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {

        Component[] components = new Component[] {
            // Pages
            Component.create(MainPage.class),
            Component.create(AccountPage.class),
            Component.create(SessionWizardPage.class),
            // Facades
            Component.create(StateFacade.class),
            Component.create(AccountStoreFacade.class),
            // IDE_embedding
            Component.create(BrowserCreator.class),
            // Manager and Helper
            Component.create(ProjectListManager.class),
            Component.create(BrowserManager.class),
            // Renderer
            Component.create(StateRenderer.class),
            Component.create(AccountRenderer.class),
            Component.create(ProjectListRenderer.class),
            // BrowserFunctions
            // contact specific
            Component.create(AddContact.class),
            Component.create(DeleteContact.class),
            Component.create(RenameContact.class),
            // dialog specific
            Component.create(ShowAccountPage.class),
            Component.create(CloseAccountWizard.class),
            Component.create(ShowSessionWizard.class),
            Component.create(CloseSessionInvitationWizard.class),
            // TODO: Eliminate inconsistent naming
            // "ShowAccountPage" <-> "CloseAccountWizard".
            // "ShowSessionWizard <-> "CloseSessionInvitationWizard". Don't
            // forget ui.frontend

            // account specific
            Component.create(ConnectAccount.class),
            Component.create(DisconnectAccount.class),
            Component.create(SaveAccount.class),
            // TODO: Add BrowserFunctions for alter accounts: "DeleteAccount"
            // and "EditAccount"

            // session specific
            Component.create(SendInvitation.class),
            // TODO: add other session related browser functions like close
            // session, add project to session, start chat, etc.

            // other
            Component.create(GetValidJID.class) };

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
