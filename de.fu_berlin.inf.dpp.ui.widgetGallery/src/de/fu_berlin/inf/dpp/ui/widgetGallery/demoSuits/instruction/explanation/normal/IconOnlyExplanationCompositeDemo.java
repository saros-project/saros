package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.ExplanationComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

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
