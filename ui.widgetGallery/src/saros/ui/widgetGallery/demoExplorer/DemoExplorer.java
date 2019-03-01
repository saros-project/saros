package saros.ui.widgetGallery.demoExplorer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import saros.ui.model.TreeLabelProvider;
import saros.ui.util.LayoutUtils;
import saros.ui.util.ViewerUtils;
import saros.ui.widgetGallery.demoSuits.MainDemo;
import saros.ui.widgets.viewer.ViewerComposite;

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
