package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanationComposite.SimpleExplanation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

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
