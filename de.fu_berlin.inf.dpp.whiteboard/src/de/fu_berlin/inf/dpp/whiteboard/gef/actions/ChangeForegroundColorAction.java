package de.fu_berlin.inf.dpp.whiteboard.gef.actions;

import de.fu_berlin.inf.dpp.ui.actions.ChangeColorAction;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.ColorUtils;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.IconUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

/**
 * Handles the action of changing foreground color. They are added in the WhiteboardEditor,
 * WhiteboardView and im Standalone Package in the WhiteboardContextMenuProvider and Whiteboard
 * ActionBarContributor
 *
 * @author Christian
 */
public class ChangeForegroundColorAction extends Action implements ColorListener {

  public static final String ACTION_ID = "foregroundColor";
  public static final String ACTION_TEXT = "Foreground Color";
  private static final Logger log = Logger.getLogger(ChangeColorAction.class);

  /**
   * The Constructor sets the ID, displayed text and the displayed image. Don't know why but it has
   * to be set twice, otherwise only a black image is seen.
   */
  public ChangeForegroundColorAction() {
    super();
    setId(ACTION_ID);
    setText(ACTION_TEXT);
    setImageDescriptor(
        ImageDescriptor.createFromImage(
            new Image(
                null,
                ImageDescriptor.createFromImage(IconUtils.getForegroundColorImage())
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
            ColorUtils.setForegroundColor(selectedColor);
          }
        });
  }

  @Override
  public void updateColor(RGB foreGround, RGB backGround) {
    setImageDescriptor(
        ImageDescriptor.createFromImage(
            new Image(
                null,
                ImageDescriptor.createFromImage(IconUtils.getForegroundColorImage())
                    .getImageData())));
  }
}
