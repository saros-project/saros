package de.fu_berlin.inf.dpp.whiteboard.gef.tools;

import org.eclipse.gef.tools.CreationTool;

/**
 * Same like GEF's CreationTool but after creating a new element it will be
 * selected automatically. Anyway this does not make sense in this whiteboard's
 * context as the CreationFactory creates a different object then will be
 * created by the command.
 * 
 * @author jurke
 * 
 */
public class CreationToolWithoutSelection extends CreationTool {

	@Override
	protected void performCreation(int button) {
		executeCurrentCommand();
		if (getCurrentViewer() != null)
			getCurrentViewer().deselectAll();
	}

}
