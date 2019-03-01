package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.ExplanationComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

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
