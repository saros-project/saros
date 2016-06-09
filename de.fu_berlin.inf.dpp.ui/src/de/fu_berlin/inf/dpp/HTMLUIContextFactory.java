package de.fu_berlin.inf.dpp;

import static de.fu_berlin.inf.dpp.AbstractSarosContextFactory.Component.create;

import java.util.Arrays;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.ui.browser_functions.AddContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunctions;
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
        add(new Component[] { 
            create(AddContact.class), 
            create(BrowserFunctions.class), // List of all BFs
            create(CloseAccountWizard.class),
            create(CloseSessionInvitationWizard.class),
            create(ConnectAccount.class),
            // create(DeleteAccount.class), //TODO: Will be added
            create(DeleteContact.class), 
            create(DisconnectAccount.class),
            // create(EditAccount.class), //TODO: Will be added
            create(GetValidJID.class), 
            create(RenameContact.class),
            create(SaveAccount.class), 
            create(SendInvitation.class),
            // create(SetActiveAccount.class), //TODO: Will be added
            create(ShowAccountPage.class), 
            create(ShowSessionWizard.class) });
    }

    private void createPages() {
        add(new Component[] { 
            create(AccountPage.class),
            create(MainPage.class), 
            create(SessionWizardPage.class) });
    }

    private void createRenderer() {
        add(new Component[] { 
            create(AccountRenderer.class),
            create(StateRenderer.class), 
            create(ProjectListRenderer.class) });
    }

    private void createFacades() {
        add(new Component[] { 
            create(AccountStoreFacade.class),
            create(StateFacade.class) });
    }

    /**
     * For UI components that fits no where else.
     */
    private void createMisc() {
        // TODO: Dodgy naming
        add(new Component[] { 
            create(BrowserCreator.class),
            create(BrowserManager.class), 
            create(ProjectListManager.class) });
    }

    /**
     * Add the components to the container
     * 
     * @param components
     *            to add
     */
    private void add(Component[] components) {
        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
