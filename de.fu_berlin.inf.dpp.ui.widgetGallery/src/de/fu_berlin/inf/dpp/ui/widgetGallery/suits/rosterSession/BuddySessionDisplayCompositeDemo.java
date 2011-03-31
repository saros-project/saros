package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.rosterSession;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BuddyDisplayComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.rosterSession.BuddySessionDisplayComposite;

public class BuddySessionDisplayCompositeDemo extends DescriptiveDemo {
	public BuddySessionDisplayCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	public String getDescription() {
		return "This "
				+ BuddyDisplayComposite.class.getSimpleName()
				+ " is registered as a "
				+ IPostSelectionProvider.class.getSimpleName()
				+ ".\n"
				+ "If you selected buddies and switch to another buddy demo you\n"
				+ "will notice that the selection has been updated there, too.";
	}

	protected Label selectedJIDs;

	@Override
	public void createContent(final Composite parent) {
		parent.setLayout(LayoutUtils.createGridLayout());

		BuddySessionDisplayComposite buddySessionDisplayComposite = new BuddySessionDisplayComposite(
				parent, SWT.BORDER | SWT.MULTI);
		buddySessionDisplayComposite.setLayoutData(LayoutUtils.createFillGridData());
	}
}
