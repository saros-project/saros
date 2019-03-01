package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits;

import de.fu_berlin.inf.dpp.ui.widgetGallery.ImageManager;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.basic.BasicDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.chat.ChatDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.decoration.DecorationDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.InstructionDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.preferences.PreferencesDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.roster.RosterDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.rosterSession.RosterSessionDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.WizardDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite;
import de.fu_berlin.inf.dpp.ui.widgets.ListExplanationComposite.ListExplanation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({
  DecorationDemoSuite.class,
  BasicDemoSuite.class,
  InstructionDemoSuite.class,
  ChatDemoSuite.class,
  RosterDemoSuite.class,
  RosterSessionDemoSuite.class,
  WizardDemoSuite.class,
  PreferencesDemoSuite.class
})
@Demo
public class MainDemo extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    ListExplanationComposite listExplanationComposite =
        new ListExplanationComposite(parent, SWT.NONE);
    ListExplanation listItemExplanation =
        new ListExplanation(
            ImageManager.WIDGET_GALLERY_32,
            "Welcome to the Saros Widget Gallery. In order to work with this Plugin...",
            "... check out existing widgets in the demo explorer on the left.",
            "... implement your own widgets by implementing your own demos.",
            "... make use of the Refresh feature (F5).");
    listExplanationComposite.setExplanation(listItemExplanation);
  }
}
