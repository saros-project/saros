package saros.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.ExplanationComposite;

@Demo
public class ExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE, SWT.ICON_INFORMATION);
    expl.setLayout(LayoutUtils.createGridLayout());
    Button explContent_hide = new Button(expl, SWT.PUSH);
    explContent_hide.setText("I'm a button explanation.");
  }
}
