package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.project;

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
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.BaseProjectSelectionComposite;

@Demo("This demo show a BaseProjectSelectionComposite that reflects the currently selected projects in the workbench.")
public class BaseProjectSelectionCompositeDemo extends AbstractDemo {
    protected BaseProjectSelectionComposite baseProjectSelectionComposite;

    @Override
    public void createDemo(Composite parent) {
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
