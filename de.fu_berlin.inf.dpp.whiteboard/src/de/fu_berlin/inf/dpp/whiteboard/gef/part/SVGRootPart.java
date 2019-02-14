package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.MarqueeDragTracker;

/**
 * The root controller that creates a white rectangular figure.<br>
 * A MarqueeDragTracker improves usability to get a marquee selection tool when dragging on the root
 * element.
 *
 * @author jurke
 */
public class SVGRootPart extends SVGRectPart {

  @Override
  protected IFigure createFigure() {
    IFigure figure = new FreeformLayer();
    figure.setLayoutManager(new FreeformLayout());

    return figure;
  }

  /** The root cannot be dragged nor selected but a marquee tool would be useful */
  @Override
  public DragTracker getDragTracker(Request request) {
    return new MarqueeDragTracker();
  }
}
