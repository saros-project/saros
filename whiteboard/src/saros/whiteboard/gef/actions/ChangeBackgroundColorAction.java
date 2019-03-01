package saros.whiteboard.gef.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import saros.ui.actions.ChangeColorAction;
import saros.ui.util.SWTUtils;
import saros.whiteboard.gef.util.ColorUtils;
import saros.whiteboard.gef.util.IconUtils;

/**
 * Handles the action of changing background color. They are added in the WhiteboardEditor,
 * WhiteboardView and im Standalone Package in the WhiteboardContextMenuProvider and Whiteboard
 * ActionBarContributor
 *
 * @author Christian
 */
public class ChangeBackgroundColorAction extends Action implements ColorListener {
  private static final Logger log = Logger.getLogger(ChangeColorAction.class);

  public static final String ACTION_ID = "backgroundColor";
  public static final String ACTION_TEXT = "Background Color";

  /**
   * The Constructor sets the ID, displayed text and the displayed image. Don't know why but it has
   * to be set twice, otherwise only a black image is seen.
   */
  public ChangeBackgroundColorAction() {
    super();
    setId(ACTION_ID);
    setText(ACTION_TEXT);

    setImageDescriptor(
        ImageDescriptor.createFromImage(
            new Image(
                null,
                ImageDescriptor.createFromImage(IconUtils.getBackgroundColorImage())
                    .getImageData())));

    ColorUtils.addListener(this);
  }

  @Override
  protected void finalize() throws Throwable {
    // TODO Auto-generated method stub
    ColorUtils.removeListener(this);
    super.finalize();
  }

  /**
   * Take care of the color change dialog and set the changed color. Furthermore it changes the
   * displayed Image
   */
  @Override
  public void run() {
    SWTUtils.runSafeSWTSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            ColorDialog changeColor = new ColorDialog(SWTUtils.getShell());
            RGB selectedColor = changeColor.open();
            if (selectedColor == null) {
              return;
            }
            ColorUtils.setBackgroundColor(selectedColor);
          }
        });
  }

  @Override
  public void updateColor(RGB foreGround, RGB backGround) {
    setImageDescriptor(
        ImageDescriptor.createFromImage(
            new Image(
                null,
                ImageDescriptor.createFromImage(IconUtils.getBackgroundColorImage())
                    .getImageData())));
  }
}
