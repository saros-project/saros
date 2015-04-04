package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import de.fu_berlin.inf.dpp.ui.ide_embedding.BrowserCreator;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddAccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContactBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.SarosMainPageBrowserFunctions;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.core_services.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountPage;
import de.fu_berlin.inf.dpp.ui.view_parts.AddContactPage;
import de.fu_berlin.inf.dpp.ui.view_parts.SarosMainPage;

/**
 * This is the HTML UI core factory for Saros. All components that are created
 * by this factory <b>must</b> be working on any platform the application is
 * running on.
 */
public class SarosHTMLUIContextFactory extends AbstractSarosContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {

        Component[] components = new Component[] {
            Component.create(SarosMainPage.class),
            Component.create(AddAccountPage.class),
            Component.create(AddContactPage.class),
            Component.create(ContactListCoreService.class),
            Component.create(AccountCoreService.class),
            Component.create(ContactListManager.class),
            Component.create(BrowserManager.class),
            Component.create(BrowserCreator.class),
            Component.create(ContactListRenderer.class),
            Component.create(AccountRenderer.class),
            Component.create(AddAccountBrowserFunctions.class),
            Component.create(AddContactBrowserFunctions.class),
            Component.create(SarosMainPageBrowserFunctions.class)
        };

        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
