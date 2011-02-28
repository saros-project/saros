package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.BaseProjectSelectionComposite;

public class BaseProjectSelectionCompositeDemo extends DescriptiveDemo {
	public BaseProjectSelectionCompositeDemo(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	protected BaseProjectSelectionComposite baseProjectSelectionComposite;

	public String getDescription() {
		return "This demo show a "
				+ BaseProjectSelectionComposite.class.getSimpleName()
				+ " that reflects the currently selected projects in the workbench.";
	}

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		baseProjectSelectionComposite = new BaseProjectSelectionComposite(
				parent, SWT.BORDER);
		baseProjectSelectionComposite.setLayoutData(new GridData(SWT.FILL,
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
		baseProjectSelectionComposite
				.setSelectedProjects(SelectionRetrieverFactory
						.getSelectionRetriever(IProject.class)
						.getOverallSelection());
	}
}
