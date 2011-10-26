package de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy;

import java.util.Arrays;

import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.ResizeTracker;

import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordAddCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordChangeLayoutCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.ElementRecordCreateCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.commands.PolylineRecordCreateCommand;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.SVGPolylineRecord;
import de.fu_berlin.inf.dpp.whiteboard.gef.part.ElementRecordPart;
import de.fu_berlin.inf.dpp.whiteboard.gef.request.CreatePointlistRequest;
import de.fu_berlin.inf.dpp.whiteboard.gef.util.LayoutUtils;

/**
 * Whiteboard application specific edit policy that returns adequate commands.
 * 
 * @author jurke
 * 
 */
public class ElementModelLayoutEditPolicy extends
		XYLayoutWithFreehandEditPolicy {

	private static Logger log = Logger
			.getLogger(ElementModelLayoutEditPolicy.class);

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

		log.trace("Create Add Command " + child);

		if (child instanceof ElementRecordPart) {
			LayoutElementRecord parent = (LayoutElementRecord) getHost()
					.getModel();

			if (child.getModel() instanceof SVGPolylineRecord) {
				parent = LayoutUtils.translateToAndGetRoot(
						(Translatable) constraint, parent);
			} else {
				parent = LayoutUtils.translateAndGetParent(
						(Translatable) constraint, parent);
			}

			// if parent is same than just change the layout
			if (parent.equals(((ElementRecordPart) child).getElementRecord()
					.getParent())) {
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
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {

		log.trace("Create Change Constraint Command " + child);

		ElementRecordChangeLayoutCommand command = null;

		if (child instanceof ElementRecordPart) {
			normalizeConstraint((Rectangle) constraint, MIN_WIDTH, MIN_HEIGHT);
			command = new ElementRecordChangeLayoutCommand();
			command.setConstraint((Rectangle) constraint);
			command.setModel(child.getModel());
		}

		return command;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {

		log.trace("Create Command " + request.getLocation() + " "
				+ request.getSize());

		if (request.getType() == REQ_CREATE
				&& getHost() instanceof ElementRecordPart) {

			LayoutElementRecord parent = (LayoutElementRecord) getHost()
					.getModel();
			Rectangle constraint = (Rectangle) getConstraintFor(request);

			// TODO fix point list figure that is wrong positioned on non-root
			/*
			 * Note, using drag and drop, the command may also create a
			 * polyline. The pencil is always added to the root.
			 */
			if (((String) request.getNewObjectType())
					.equals(SVGConstants.SVG_POLYLINE_TAG)) {
				parent = LayoutUtils.translateToAndGetRoot(constraint, parent);
			} else {
				// Take the next parent that is a composite
				parent = LayoutUtils.translateAndGetParent(constraint, parent);
			}

			/* adjust the default size to the zoom */
			double zoom = 1d;
			ZoomManager zoomManager = ((ScalableFreeformRootEditPart) getTargetEditPart(
					request).getViewer().getRootEditPart()).getZoomManager();
			if (zoomManager != null)
				zoom = zoomManager.getZoom() * zoomManager.getUIMultiplier();

			// created by Drag and Drop or click
			if (constraint.width == -1 && constraint.height == -1) {
				normalizeConstraint(constraint, (int) (DEFAULT_WIDTH / zoom),
						(int) (DEFAULT_HEIGHT / zoom));
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

	/**
	 * Returns a polyline create command that appends a polyline to the root
	 */
	@Override
	protected Command getCreatePointListCommand(CreatePointlistRequest request) {

		log.trace("Create PointList Command "
				+ Arrays.toString(request.getPoints().toIntArray()));

		if (request.getType() == REQ_CREATE_POINTLIST
				&& getHost() instanceof ElementRecordPart) {
			try {

				LayoutElementRecord parent = (LayoutElementRecord) getHost()
						.getModel();
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
	 * Custom implementation that uses a newly created object from the request's
	 * creation factory to provide a custom shape feedback
	 */
	@Override
	protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
		EditPart part = getHost().getViewer().getEditPartFactory()
				.createEditPart(getHost(), createRequest.getNewObject());
		if (part instanceof AbstractGraphicalEditPart) {
			IFigure figure = ((AbstractGraphicalEditPart) part).getFigure();
			if (figure instanceof Shape) {
				FigureUtilities.makeGhostShape((Shape) figure);
				((Shape) figure).setLineStyle(Graphics.LINE_DASHDOT);
				figure.setForegroundColor(ColorConstants.white);
			}
			addFeedback(figure);
			return figure;
		}

		return null;
	}

	/**
	 * Check and sets if applicable the width and height to the provided
	 * minimums
	 */
	protected void normalizeConstraint(Rectangle rect, int minWidth,
			int minHeight) {
		if (rect.width < minWidth)
			rect.width = minWidth;
		if (rect.height < minHeight)
			rect.height = minHeight;
	}

	/**
	 * This method generates a customized ResizableEditPolicy that provides a
	 * percentage oriented resizing mechanism for multiple selections.
	 */
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new ResizableEditPolicy() {
			protected final String PERCENTAGE_HEIGHT = "percentageHeight";
			protected final String PERCENTAGE_WIDTH = "percentageWidth";

			/**
			 * Calculates Delta values for the given child from the percentage
			 * values of the extended map of the given request.
			 * 
			 * @param request
			 * @param child
			 */
			protected void recalculateResizeData(ChangeBoundsRequest request,
					GraphicalEditPart child) {
				if (request.getExtendedData().containsKey(PERCENTAGE_HEIGHT)
						&& request.getExtendedData().containsKey(
								PERCENTAGE_WIDTH)) {
					Dimension originalSize = child.getFigure().getSize();
					// calculating size delta values
					double percentageHeight = (Double) request
							.getExtendedData().get(PERCENTAGE_HEIGHT);
					double percentageWidth = (Double) request.getExtendedData()
							.get(PERCENTAGE_WIDTH);
					int deltaHeight = (int) (originalSize.height
							* percentageHeight / 100);
					int deltaWidth = (int) (originalSize.width
							* percentageWidth / 100);
					// calculating move delta values
					int moveX = 0;
					int moveY = 0;

					// note: SOUTH and EAST directions don't need a move delta
					switch (request.getResizeDirection()) {
					// WEST direction: x adjustment
					case PositionConstants.SOUTH_WEST:
					case PositionConstants.WEST:
						moveX = -deltaWidth;
						break;
					// NORTH direction: y adjustment
					case PositionConstants.NORTH_EAST:
					case PositionConstants.NORTH:
						moveY = -deltaHeight;
						break;
					// NORTH and WEST direction: x and y adjustment
					case PositionConstants.NORTH_WEST:
						moveX = -deltaWidth;
						moveY = -deltaHeight;
						break;
					// SOUTH or EAST direction: no adjustment
					case PositionConstants.SOUTH_EAST:
					case PositionConstants.EAST:
					case PositionConstants.SOUTH:
					default:
						break;
					}
					// applying delta values
					request.setSizeDelta(new Dimension(deltaWidth, deltaHeight));
					request.setMoveDelta(new Point(moveX, moveY));
				}
			}

			@Override
			protected ResizeTracker getResizeTracker(int direction) {
				return new ResizeTracker((GraphicalEditPart) getHost(),
						direction) {

					/**
					 * Overridden to generate and save additional resize
					 * percentage information derived by the help from the
					 * ResizeTracker which are then stored in the request
					 */
					@SuppressWarnings("unchecked")
					@Override
					protected void updateSourceRequest() {
						super.updateSourceRequest();
						ChangeBoundsRequest request = (ChangeBoundsRequest) getSourceRequest();
						// note that the getOwner() method is the crucial
						// method to determine the figure that is currently
						// resized and that serves as basis for further
						// percentage calculations
						GraphicalEditPart dragSource = getOwner();
						Dimension sizeDelta = request.getSizeDelta();
						Dimension originalSize = dragSource.getFigure()
								.getSize();
						// calculating percentage resize values, for maximum
						// precision we save the result as double value
						double percentageHeight = 100d * sizeDelta.height
								/ originalSize.height;
						double percentageWidth = 100d * sizeDelta.width
								/ originalSize.width;
						// saving percentage information
						request.getExtendedData().put(PERCENTAGE_HEIGHT,
								percentageHeight);
						request.getExtendedData().put(PERCENTAGE_WIDTH,
								percentageWidth);
					}
				};
			}

			/**
			 * Overridden to adapt the feedback to the new percentage oriented
			 * resizing mechanism.
			 */
			@Override
			protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
				recalculateResizeData(request, (GraphicalEditPart) getHost());
				super.showChangeBoundsFeedback(request);
			}

			/**
			 * This method applies the percentage information on the given
			 * request for the host of this edit policy. Note that it is
			 * necessary to recalculate the size information for every child
			 * since a request only stores one global delta information for
			 * multiple EditParts.
			 */
			@Override
			protected Command getResizeCommand(ChangeBoundsRequest request) {
				recalculateResizeData(request, (GraphicalEditPart) getHost());
				return super.getResizeCommand(request);
			}
		};

	}

}
