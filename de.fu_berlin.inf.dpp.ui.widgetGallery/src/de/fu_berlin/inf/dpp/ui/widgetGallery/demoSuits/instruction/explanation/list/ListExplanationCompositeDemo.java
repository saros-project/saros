package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.list;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite.ListExplanation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

@Demo
public class ListExplanationCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    ListExplanationComposite listExplanationComposite =
        new ListExplanationComposite(parent, SWT.NONE);
    ListExplanation listItemExplanation =
        new ListExplanation(
            SWT.ICON_INFORMATION,
            "I'm the introductory text...",
            "List item 1",
            "List item 2",
            "List item 3",
            "List item 4",
            "List item 5",
            "List item 6",
            "List item 7",
            "List item 8",
            "List item 9",
            "List item 10",
            "List item 11",
            "List item 12",
            "List item 13",
            "List item 14",
            "List item 15");
    listExplanationComposite.setExplanation(listItemExplanation);
  }
}
