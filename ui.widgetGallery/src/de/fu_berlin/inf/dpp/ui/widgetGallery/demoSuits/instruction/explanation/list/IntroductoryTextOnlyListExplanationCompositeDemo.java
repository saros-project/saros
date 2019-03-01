package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.list;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite.ListExplanation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

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
