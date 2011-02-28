package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.roster;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BaseBuddySelectionComposite;

public class BaseBuddySelectionCompositeDemo extends DescriptiveDemo {
	public BaseBuddySelectionCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	protected BaseBuddySelectionComposite baseBuddySelectionComposite;

	public String getDescription() {
		return "This demo show a "
				+ BaseBuddySelectionComposite.class.getSimpleName()
				+ " that reflects the currently selected buddies in the workbench.";
	}

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		baseBuddySelectionComposite = new BaseBuddySelectionComposite(parent,
				SWT.BORDER);
		baseBuddySelectionComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, true));
		SelectionUtils.getSelectionService().addSelectionListener(
				new ISelectionListener() {
					public void selectionChanged(IWorkbenchPart part,
							ISelection selection) {
						updateSelection();
					}
				});
	}

	protected void updateSelection() {
		baseBuddySelectionComposite
				.setSelectedBuddies(SelectionRetrieverFactory
						.getSelectionRetriever(JID.class).getOverallSelection());
	}
}
