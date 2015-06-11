package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.ui.browser_functions.AddAccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContactBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.MainPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.SessionWizardPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.core_facades.AccountStoreFacade;
import de.fu_berlin.inf.dpp.ui.core_facades.ContactListFacade;
import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.webpages.AddAccountPage;
import de.fu_berlin.inf.dpp.ui.webpages.AddContactPage;
import de.fu_berlin.inf.dpp.ui.webpages.MainPage;
import de.fu_berlin.inf.dpp.ui.webpages.SessionWizardPage;

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
            Component.create(MainPage.class),
            Component.create(AddAccountPage.class),
            Component.create(AddContactPage.class),
            Component.create(SessionWizardPage.class),
            Component.create(ContactListFacade.class),
            Component.create(AccountStoreFacade.class),
            Component.create(ContactListManager.class),
            Component.create(BrowserManager.class),
            Component.create(BrowserCreator.class),
            Component.create(ContactListRenderer.class),
            Component.create(AccountRenderer.class),
            Component.create(AddAccountBrowserFunctions.class),
            Component.create(AddContactBrowserFunctions.class),
            Component.create(MainPageBrowserFunctions.class),
            Component.create(SessionWizardPageBrowserFunctions.class) };

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
