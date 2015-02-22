package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.core_services.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.renderer.SarosMainPageRenderer;
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
            Component.create(SarosMainPageRenderer.class),
            Component.create(ContactListRenderer.class),
            Component.create(AccountRenderer.class) };
        for (Component component : Arrays.asList(components)) {
            container.addComponent(component.getBindKey(),
                component.getImplementation());
        }
    }
}
