package de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;

/**
 * This class is an extended ResizableEditPolicy with ratio scaling when dealing
 * with multiple selections.
 * 
 * @author hdegener
 * 
 */
public class ResizableRatioScalingEditPolicy extends ResizableEditPolicy {
	protected final String PERCENTAGE_HEIGHT = "percentageHeight";
	protected final String PERCENTAGE_WIDTH = "percentageWidth";

	/**
	 * Calculates Delta values for the given child from the percentage values of
	 * the extended map of the given request.
	 * 
	 * @param request
	 * @param child
	 */
	protected void recalculateResizeData(ChangeBoundsRequest request,
			GraphicalEditPart child) {
		if (request.getExtendedData().containsKey(PERCENTAGE_HEIGHT)
				&& request.getExtendedData().containsKey(PERCENTAGE_WIDTH)) {
			Dimension originalSize = child.getFigure().getSize();
			// calculating size delta values
			double percentageHeight = (Double) request.getExtendedData().get(
					PERCENTAGE_HEIGHT);
			double percentageWidth = (Double) request.getExtendedData().get(
					PERCENTAGE_WIDTH);
			int deltaHeight = (int) (originalSize.height * percentageHeight / 100);
			int deltaWidth = (int) (originalSize.width * percentageWidth / 100);
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
		return new ResizeTracker((GraphicalEditPart) getHost(), direction) {

			/**
			 * Overridden to generate and save additional resize percentage
			 * information derived by the help from the ResizeTracker which are
			 * then stored in the request
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
				Dimension originalSize = dragSource.getFigure().getSize();
				// calculating percentage resize values, for maximum
				// precision we save the result as double value
				double percentageHeight = 100d * sizeDelta.height
						/ originalSize.height;
				double percentageWidth = 100d * sizeDelta.width
						/ originalSize.width;
				// saving percentage information
				request.getExtendedData().put(PERCENTAGE_HEIGHT,
						percentageHeight);
				request.getExtendedData()
						.put(PERCENTAGE_WIDTH, percentageWidth);
			}
		};
	}

	/**
	 * Overridden to adapt the feedback to the new percentage oriented resizing
	 * mechanism.
	 */
	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		recalculateResizeData(request, (GraphicalEditPart) getHost());
		super.showChangeBoundsFeedback(request);
	}

	/**
	 * This method applies the percentage information on the given request for
	 * the host of this edit policy. Note that it is necessary to recalculate
	 * the size information for every child since a request only stores one
	 * global delta information for multiple EditParts.
	 */
	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {
		recalculateResizeData(request, (GraphicalEditPart) getHost());
		return super.getResizeCommand(request);
	}
}
