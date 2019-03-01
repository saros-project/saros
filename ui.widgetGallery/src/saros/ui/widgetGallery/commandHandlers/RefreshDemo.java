package saros.ui.widgetGallery.commandHandlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import saros.ui.widgetGallery.views.WidgetGalleryView;

public class RefreshDemo extends AbstractHandler {

  protected final Logger log = Logger.getLogger(ShowWidgetGalleryView.class);

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (part instanceof WidgetGalleryView) {
      WidgetGalleryView view = (WidgetGalleryView) part;
      view.openDemo();
    }
    return null;
  }
}
