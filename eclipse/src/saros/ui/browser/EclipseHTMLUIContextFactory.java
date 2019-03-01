package saros.ui.browser;

import org.picocontainer.MutablePicoContainer;
import saros.HTMLUIContextFactory;
import saros.context.AbstractContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.ide_embedding.IUIResourceLocator;
import saros.ui.util.ICollaborationUtils;
import saros.util.EclipseCollaborationUtilsImpl;

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
