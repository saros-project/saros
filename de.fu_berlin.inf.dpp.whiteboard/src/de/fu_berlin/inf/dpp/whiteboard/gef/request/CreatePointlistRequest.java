package de.fu_berlin.inf.dpp.whiteboard.gef.request;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.requests.CreateRequest;

import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.XYLayoutWithFreehandEditPolicy;

/**
 * Simple request class to track points
 * 
 * @author jurke
 * 
 */
public class CreatePointlistRequest extends CreateRequest {

	private PointList points = null;

	public CreatePointlistRequest() {
		setType(XYLayoutWithFreehandEditPolicy.REQ_CREATE_POINTLIST);
	}

	public PointList getPoints() {
		return points;
	}

	public void addPoint(Point p) {
		if (points == null)
			points = new PointList();
		points.addPoint(p);
	}

	@Override
	public Point getLocation() {
		if (points == null)
			return null;
		return points.getBounds().getLocation();
	}

	@Override
	public void setLocation(Point location) {
		clear();
		addPoint(location);
	}

	@Override
	public Dimension getSize() {
		return points.getBounds().getSize();
	}

	public void clear() {
		points = null;
	}

}
