package saros.whiteboard.gef.actions;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import saros.whiteboard.messages.WhiteboardMessages;

public class ExportToImageAction extends WorkbenchPartAction {

  private int format;
  private String filename;

  public ExportToImageAction(IWorkbenchPart part) {
    super(part);
  }

  /** setting id, image and description */
  @Override
  protected void init() {
    setId(ActionFactory.EXPORT.getId());
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
    setDisabledImageDescriptor(
        sharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
    setDescription(WhiteboardMessages.export_description);
    setText(WhiteboardMessages.export_text);
    setEnabled(false);
  }

  /** Action is enabled when the whiteboard is not empty */
  @Override
  protected boolean calculateEnabled() {
    GraphicalViewer viewer =
        ((GraphicalViewer) getWorkbenchPart().getAdapter(GraphicalViewer.class));
    return !(viewer == null
        || viewer.getContents() == null
        || viewer.getContents().getChildren().isEmpty());
  }

  /** executes this action */
  @Override
  public void run() {
    obtainFilenameAndFormat();
    export();
  }

  private void export() {
    GraphicalViewer graphicalViewer =
        (GraphicalViewer) getWorkbenchPart().getAdapter(GraphicalViewer.class);
    LayerManager layerManager =
        (LayerManager) graphicalViewer.getEditPartRegistry().get(LayerManager.ID);
    IFigure rootFigure = layerManager.getLayer(LayerConstants.PRINTABLE_LAYERS);
    exportImage(format, filename, rootFigure);
  }

  /** opens an export dialog, setting format and filename values. */
  private void obtainFilenameAndFormat() {
    FileDialog exportDialog = new FileDialog(new Shell(Display.getDefault()), SWT.SAVE);
    exportDialog.setOverwrite(true);
    exportDialog.setText(WhiteboardMessages.export_dialog_text);
    String[] extensions = new String[] {"*.png", "*.gif", "*.jpg"};
    exportDialog.setFilterExtensions(extensions);
    filename = exportDialog.open();
    switch (exportDialog.getFilterIndex()) {
      case 0:
        format = SWT.IMAGE_PNG;
        break;
      case 1:
        format = SWT.IMAGE_GIF;
        break;
      case 2:
        format = SWT.IMAGE_JPEG;
        break;
      default:
        return;
    }
  }

  public static void exportImage(int format, String filename, IFigure rootFigure) {
    // get the bounding rectangle around the figure's children
    Rectangle boundingBox = getBoundingBox(rootFigure);

    // don't export if the canvas is empty
    if (boundingBox == null) return;

    // obtain a painter
    Image diagramImage = new Image(Display.getDefault(), boundingBox.width, boundingBox.height);
    GC gc = new GC(diagramImage);
    SWTGraphics swtGraphics = new SWTGraphics(gc);
    // place the painter to the boundingBox's (0,0)
    swtGraphics.translate(-boundingBox.x, -boundingBox.y);

    paintChildren(swtGraphics, rootFigure);

    ImageLoader imgLoader = new ImageLoader();
    imgLoader.data = new ImageData[] {diagramImage.getImageData()};
    imgLoader.save(filename, format);

    swtGraphics.dispose();
    gc.dispose();
    diagramImage.dispose();
  }

  private static Rectangle getBoundingBox(IFigure figure) {
    Rectangle boundingBox = null;
    for (Object layer : figure.getChildren()) {
      Rectangle bounds;
      if (layer instanceof FreeformLayer) {
        bounds = getBoundingBox((IFigure) layer);
      } else {
        bounds = ((IFigure) layer).getBounds().getCopy();
      }
      if (boundingBox != null) {
        boundingBox.union(bounds);
      } else {
        boundingBox = bounds;
      }
    }

    return boundingBox;
  }

  private static void paintChildren(Graphics g, IFigure figure) {
    for (Object child : figure.getChildren()) {
      // ConnectionLayer does not contain children -> paint directly
      // else paint Children
      if (child instanceof FreeformLayer && !(child instanceof ConnectionLayer)) {
        paintChildren(g, (IFigure) child);
      } else {
        ((IFigure) child).paint(g);
      }
    }
  }
}
