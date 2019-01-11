package de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordAddCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordChangeLayoutCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordCreateCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.PolylineRecordCreateCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.TextboxCreateCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGPolylineRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.part.ElementRecordPart;
import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreatePointlistRequest;
import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreateTextBoxRequest;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.ColorUtils;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.LayoutUtils;
import java.util.Arrays;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.graphics.Color;

/**
 * Whiteboard application specific edit policy that returns adequate commands.
 *
 * @author jurke
 */
public class ElementModelLayoutEditPolicy extends XYLayoutWithFreehandEditPolicy {

  private static Logger log = Logger.getLogger(ElementModelLayoutEditPolicy.class);

  /* minimum values for resizing and drag creation */
  public static final int MIN_WIDTH = 1;
  public static final int MIN_HEIGHT = 1;
  /* default size for click and drag and drop creation */
  public static final int DEFAULT_WIDTH = 40;
  public static final int DEFAULT_HEIGHT = 40;

  /*
   * Paraphrase: change (or rather "add to another") parent command
   */
  @Override
  protected Command createAddCommand(EditPart child, Object constraint) {
    ElementRecordAddCommand command = null;

    if (log.isTraceEnabled()) log.trace("Create Add Command " + child);

    if (child instanceof ElementRecordPart) {
      LayoutElementRecord parent = (LayoutElementRecord) getHost().getModel();

      if (child.getModel() instanceof SVGPolylineRecord) {
        parent = LayoutUtils.translateToAndGetRoot((Translatable) constraint, parent);
      } else {
        parent = LayoutUtils.translateAndGetParent((Translatable) constraint, parent);
      }

      // if parent is same than just change the layout
      if (parent.equals(((ElementRecordPart) child).getElementRecord().getParent())) {
        return createChangeConstraintCommand(child, constraint);
      }

      command = new ElementRecordAddCommand();
      command.setElementModel(child.getModel());
      command.setParent(parent);
      command.setLayout((Rectangle) constraint);
    }

    return command;
  }

  /*
   * Paraphrase: change layout command
   */
  @Override
  protected Command createChangeConstraintCommand(EditPart child, Object constraint) {

    if (log.isTraceEnabled()) log.trace("Create Change Constraint Command " + child);

    ElementRecordChangeLayoutCommand command = null;

    if (child instanceof ElementRecordPart) {
      normalizeConstraint((Rectangle) constraint, MIN_WIDTH, MIN_HEIGHT);
      command = new ElementRecordChangeLayoutCommand();
      command.setConstraint((Rectangle) constraint);
      command.setModel(child.getModel());
    }

    return command;
  }

  /** is called every time the mouse moves in the editor */
  @Override
  protected Command getCreateCommand(CreateRequest request) {

    if (log.isTraceEnabled())
      log.trace("Create Command " + request.getLocation() + " " + request.getSize());

    if (request.getType() == REQ_CREATE && getHost() instanceof ElementRecordPart) {

      LayoutElementRecord parent = (LayoutElementRecord) getHost().getModel();
      Rectangle constraint = (Rectangle) getConstraintFor(request);

      // TODO fix point list figure that is wrong positioned on non-root
      /*
       * Note, using drag and drop, the command may also create a
       * polyline. The pencil is always added to the root.
       */
      if (((String) request.getNewObjectType()).equals(SVGConstants.SVG_POLYLINE_TAG)) {
        parent = LayoutUtils.translateToAndGetRoot(constraint, parent);
      } else {
        // Take the next parent that is a composite
        parent = LayoutUtils.translateAndGetParent(constraint, parent);
      }

      /* adjust the default size to the zoom */
      double zoom = 1d;
      ZoomManager zoomManager =
          ((ScalableFreeformRootEditPart) getTargetEditPart(request).getViewer().getRootEditPart())
              .getZoomManager();
      if (zoomManager != null) zoom = zoomManager.getZoom() * zoomManager.getUIMultiplier();

      // created by Drag and Drop or click
      if (constraint.width == -1 && constraint.height == -1) {
        normalizeConstraint(
            constraint, (int) (DEFAULT_WIDTH / zoom), (int) (DEFAULT_HEIGHT / zoom));
      }
      // created by selection rectangle
      else {
        normalizeConstraint(constraint, MIN_WIDTH, MIN_HEIGHT);
      }

      ElementRecordCreateCommand cmd = new ElementRecordCreateCommand();

      cmd.setParent(parent);
      cmd.setChildName((String) request.getNewObjectType());
      cmd.setLayout(constraint);

      return cmd;
    }
    return null;
  }

  @Override
  protected Command getCreateTextboxCommand(CreateTextBoxRequest request) {

    if (log.isTraceEnabled())
      log.trace("Create Textbox Command " + request.getLocation() + " " + request.getSize());

    if (request.getType() == REQ_CREATE_TEXTBOX && getHost() instanceof ElementRecordPart) {

      LayoutElementRecord parent = (LayoutElementRecord) getHost().getModel();
      Rectangle constraint = (Rectangle) getConstraintFor(request);

      // Take the next parent that is a composite
      parent = LayoutUtils.translateAndGetParent(constraint, parent);

      /* adjust the default size to the zoom */
      double zoom = 1d;
      ZoomManager zoomManager =
          ((ScalableFreeformRootEditPart) getTargetEditPart(request).getViewer().getRootEditPart())
              .getZoomManager();
      if (zoomManager != null) zoom = zoomManager.getZoom() * zoomManager.getUIMultiplier();

      // created by Drag and Drop or click
      if (constraint.width == -1 && constraint.height == -1) {
        normalizeConstraint(
            constraint, (int) (DEFAULT_WIDTH / zoom), (int) (DEFAULT_HEIGHT / zoom));
      }
      // created by selection rectangle
      else {
        normalizeConstraint(constraint, MIN_WIDTH, MIN_HEIGHT);
      }
      TextboxCreateCommand cmd = new TextboxCreateCommand();

      cmd.setParent(parent);
      cmd.setChildName((String) request.getNewObjectType());
      cmd.setLayout(constraint);
      cmd.setText(request.getText());

      return cmd;
    }
    return null;
  }

  /** Returns a polyline create command that appends a polyline to the root */
  @Override
  protected Command getCreatePointListCommand(CreatePointlistRequest request) {

    if (log.isTraceEnabled())
      log.trace(
          request.getType()
              + " Create PointList Command "
              + Arrays.toString(request.getPoints().toIntArray()));

    if ((request.getType() == REQ_CREATE_POINTLIST || request.getType() == REQ_CREATE_LINE)
        && getHost() instanceof ElementRecordPart) {
      try {

        LayoutElementRecord parent = (LayoutElementRecord) getHost().getModel();
        PointList points = getPointListFor(request);

        // pencil on root only
        parent = LayoutUtils.translateToAndGetRoot(points, parent);

        PolylineRecordCreateCommand cmd = new PolylineRecordCreateCommand();

        cmd.setParent(parent);
        cmd.setPointList(points);

        return cmd;

      } catch (Exception e) {
        log.error("Error creating PolylineModelCreateCommand.", e);
      }
    }
    return null;
  }

  /**
   * Custom implementation that uses a newly created object from the request's creation factory to
   * provide a custom shape feedback
   */
  @Override
  protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
    EditPart part =
        getHost()
            .getViewer()
            .getEditPartFactory()
            .createEditPart(getHost(), createRequest.getNewObject());

    if (log.isTraceEnabled())
      log.trace(
          "Create Size on drop feedback EditPart:"
              + part
              + " is AbstractGraphicalEditPart:"
              + (part instanceof AbstractGraphicalEditPart));

    if (part instanceof AbstractGraphicalEditPart) {
      IFigure figure = ((AbstractGraphicalEditPart) part).getFigure();
      figure.setForegroundColor(new Color(null, ColorUtils.getRGBForegroundColor()));
      figure.setBackgroundColor(new Color(null, ColorUtils.getRGBBackgroundColor()));

      if (log.isTraceEnabled()) {
        log.trace("Figure is Shape: " + (figure instanceof Shape));
        log.trace(
            "Figure BG Color:"
                + figure.getBackgroundColor()
                + " Foreground Color:"
                + figure.getForegroundColor());
      }
      addFeedback(figure);
      return figure;
    } else
      log.debug("Part is no instance of AbstractGraphicalEditPart: " + part.getClass().toString());

    return null;
  }

  /** Check and sets if applicable the width and height to the provided minimums */
  protected void normalizeConstraint(Rectangle rect, int minWidth, int minHeight) {
    if (rect.width < minWidth) rect.width = minWidth;
    if (rect.height < minHeight) rect.height = minHeight;
  }

  /**
   * This method returns a ResizableRatioScalingEditPolicy thus providing a percentage oriented
   * resizing mechanism for multiple selections.
   */
  @Override
  protected EditPolicy createChildEditPolicy(EditPart child) {
    return new ResizableRatioScalingEditPolicy();
  }
}
