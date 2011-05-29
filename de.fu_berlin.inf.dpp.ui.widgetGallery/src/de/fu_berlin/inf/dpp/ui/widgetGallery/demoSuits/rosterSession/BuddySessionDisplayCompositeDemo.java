package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.rosterSession;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession.BuddySessionDisplayComposite;

@Demo("This BuddyDisplayComposite IPostSelectionProvider.\nIf you selected buddies and switch to another buddy demo you\n"
	+ "will notice that the selection has been updated there, too.")
public class BuddySessionDisplayCompositeDemo extends AbstractDemo {
    protected Label selectedJIDs;

    @Override
    public void createDemo(final Composite parent) {
	parent.setLayout(LayoutUtils.createGridLayout());

	BuddySessionDisplayComposite buddySessionDisplayComposite = new BuddySessionDisplayComposite(
		parent, SWT.BORDER | SWT.MULTI);
	buddySessionDisplayComposite.setLayoutData(LayoutUtils
		.createFillGridData());
    }
}
