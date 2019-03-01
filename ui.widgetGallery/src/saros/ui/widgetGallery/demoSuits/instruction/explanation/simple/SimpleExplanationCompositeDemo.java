package saros.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.SimpleExplanationComposite;
import saros.ui.widgets.SimpleExplanationComposite.SimpleExplanation;

@Demo
public class SimpleExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    SimpleExplanationComposite simpleExplanationComposite =
        new SimpleExplanationComposite(parent, SWT.NONE);
    SimpleExplanation simpleExplanation =
        new SimpleExplanation(SWT.ICON_INFORMATION, "This is a simple explanation.");
    simpleExplanationComposite.setExplanation(simpleExplanation);
  }
}
