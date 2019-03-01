package saros.ui.widgetGallery.commandHandlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import saros.ui.widgetGallery.views.WidgetGalleryView;

public class ShowWidgetGalleryView extends AbstractHandler {

  protected final Logger log = Logger.getLogger(ShowWidgetGalleryView.class);

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow()
          .getActivePage()
          .showView(WidgetGalleryView.ID);
    } catch (PartInitException e) {
      log.error("Error showing " + WidgetGalleryView.class.getSimpleName(), e);
    }
    return null;
  }
}
