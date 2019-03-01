package saros.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.ExplanationComposite;

@Demo
public class ExplanationOnlyExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    final ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE, null);
    expl.setLayout(new FillLayout());
    Button explContent_hide = new Button(expl, SWT.PUSH);
    explContent_hide.setText("I'm a button explanation.");
  }
}
