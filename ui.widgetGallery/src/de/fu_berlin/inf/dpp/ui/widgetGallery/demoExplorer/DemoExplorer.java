package de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer;

import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.MainDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;

public class DemoExplorer extends ViewerComposite<TreeViewer> {

  public DemoExplorer(Composite parent, int style) {
    super(parent, style);

    super.setLayout(LayoutUtils.createGridLayout());

    getViewer().getControl().setLayoutData(LayoutUtils.createFillGridData());
    getViewer().setInput(MainDemo.class);
    ViewerUtils.expandToLevel(getViewer(), 2);
  }

  @Override
  protected TreeViewer createViewer(int style) {
    return new TreeViewer(new Tree(this, style));
  }

  @Override
  protected void configureViewer(TreeViewer viewer) {
    viewer.setContentProvider(new DemoContentProvider());
    viewer.setLabelProvider(new TreeLabelProvider());
    viewer.setUseHashlookup(true);
  }

  @Override
  public void setLayout(Layout layout) {
    // ignore
  }

  public DemoElement getSelectedDemoElement() {
    DemoElement demoElement =
        (DemoElement) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
    if (demoElement == null) demoElement = new DemoElement(MainDemo.class);
    return demoElement;
  }
}
