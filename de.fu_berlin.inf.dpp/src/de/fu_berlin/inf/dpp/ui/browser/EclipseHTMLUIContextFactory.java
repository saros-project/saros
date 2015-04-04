package de.fu_berlin.inf.dpp.ui.browser;

import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.AbstractSarosContextFactory;
import de.fu_berlin.inf.dpp.SarosHTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IWebResourceLocator;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;

/**
 * Context factory that provides the necessary components that are required for
 * the {@link SarosHTMLUIContextFactory}.
 */
public class EclipseHTMLUIContextFactory extends AbstractSarosContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {
        container
            .addComponent(IDialogManager.class, EclipseDialogManager.class);
        container.addComponent(IWebResourceLocator.class,
            EclipseResourceLocator.class);
    }
}
