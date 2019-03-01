package saros.ui.widgetGallery.demoSuits;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.ImageManager;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.basic.BasicDemoSuite;
import saros.ui.widgetGallery.demoSuits.chat.ChatDemoSuite;
import saros.ui.widgetGallery.demoSuits.decoration.DecorationDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.InstructionDemoSuite;
import saros.ui.widgetGallery.demoSuits.preferences.PreferencesDemoSuite;
import saros.ui.widgetGallery.demoSuits.roster.RosterDemoSuite;
import saros.ui.widgetGallery.demoSuits.rosterSession.RosterSessionDemoSuite;
import saros.ui.widgetGallery.demoSuits.wizard.WizardDemoSuite;
import saros.ui.widgets.ListExplanationComposite;
import saros.ui.widgets.ListExplanationComposite.ListExplanation;

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
