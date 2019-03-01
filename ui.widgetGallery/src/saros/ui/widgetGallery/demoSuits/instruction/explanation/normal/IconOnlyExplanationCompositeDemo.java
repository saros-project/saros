package saros.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.ExplanationComposite;

@Demo
public class IconOnlyExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE, SWT.ICON_INFORMATION);
    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
    rowLayout.wrap = true;
    expl.setLayout(rowLayout);
  }
}
