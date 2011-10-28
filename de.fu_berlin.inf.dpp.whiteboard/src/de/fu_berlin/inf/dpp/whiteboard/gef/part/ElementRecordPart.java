package de.fu_berlin.inf.dpp.whiteboard.gef.part;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;

import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.ElementModelDeletePolicy;
import de.fu_berlin.inf.dpp.whiteboard.gef.editpolicy.ElementModelLayoutEditPolicy;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.LayoutElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ChildRecordChangeCache.ChildRecordChangeListener;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;

/**
 * Base edit part for SXE that installs the edit policies, registers itself on
 * activation and refreshes the figure or the children respective notification.
 * 
 * @author jurke
 * 
 */
public abstract class ElementRecordPart extends AbstractGraphicalEditPart
		implements ChildRecordChangeListener {

	@Override
	public void activate() {
		super.activate();
		getElementRecord().addChildRecordChangeListener(this);
	}

	@Override
	public void deactivate() {
		super.deactivate();
		getElementRecord().removeChildRecordChangeListener(this);
	}

	public LayoutElementRecord getElementRecord() {
		return (LayoutElementRecord) getModel();
	}

	// @Override
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new ElementModelLayoutEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ElementModelDeletePolicy());
	}

	@Override
	public List<ElementRecord> getModelChildren() {
		return getElementRecord().getVisibleChildElements();
	}

	@Override
	public void childElementRecordChanged(List<IRecord> records) {
		refreshChildren();
	}

	@Override
	public void attributeRecordChanged(List<IRecord> records) {
		refreshVisuals();
	}

	@Override
	protected void refreshVisuals() {
		Rectangle bounds = getElementRecord().getLayout();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), bounds);
	}

	@Override
	public void childRecordConflict(Map<NodeRecord, Set<SetRecord>> conflicts) {
		// TODO inform user
	}

	/**
	 * Returns the current zoom Level. 1 for 100%
	 * 
	 * @return
	 */
	protected double getCurrentZoom() {
		ZoomManager z = ((ScalableFreeformRootEditPart) getViewer()
				.getRootEditPart()).getZoomManager();
		return z.getZoom() * z.getUIMultiplier();
	}
}
