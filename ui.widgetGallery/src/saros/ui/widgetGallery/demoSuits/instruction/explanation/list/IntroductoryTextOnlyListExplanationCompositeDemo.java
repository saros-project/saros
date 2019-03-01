package saros.ui.widgetGallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.ListExplanationComposite;
import saros.ui.widgets.ListExplanationComposite.ListExplanation;

@Demo
public class IntroductoryTextOnlyListExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    ListExplanationComposite listExplanationComposite =
        new ListExplanationComposite(parent, SWT.NONE);
    ListExplanation listItemExplanation =
        new ListExplanation(SWT.ICON_INFORMATION, "I'm the introductory text...");
    listExplanationComposite.setExplanation(listItemExplanation);
  }
}
