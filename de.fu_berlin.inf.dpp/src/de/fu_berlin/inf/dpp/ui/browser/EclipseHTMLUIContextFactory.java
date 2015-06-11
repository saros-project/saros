package de.fu_berlin.inf.dpp.ui.browser;

import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IWebResourceLocator;

/**
 * Context factory that provides the necessary components that are required for
 * the {@link HTMLUIContextFactory}.
 */
public class EclipseHTMLUIContextFactory extends AbstractSarosContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {
        container
            .addComponent(DialogManager.class, EclipseDialogManager.class);
        container.addComponent(IWebResourceLocator.class,
            EclipseResourceLocator.class);
    }
}
