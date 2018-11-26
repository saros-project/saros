package de.fu_berlin.inf.dpp.ui.browser;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.AbstractContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IUIResourceLocator;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;
import de.fu_berlin.inf.dpp.util.EclipseCollaborationUtilsImpl;
import org.picocontainer.MutablePicoContainer;

/**
 * Context factory that provides the necessary components that are required for the {@link
 * HTMLUIContextFactory}.
 */
public class EclipseHTMLUIContextFactory extends AbstractContextFactory {
  // TODO: This extra context factory should be move to SarosContextFactory
  // when HTML UI is stable
  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(DialogManager.class, EclipseDialogManager.class);
    container.addComponent(IUIResourceLocator.class, EclipseResourceLocator.class);
    container.addComponent(ICollaborationUtils.class, EclipseCollaborationUtilsImpl.class);
  }
}
