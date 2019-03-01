package saros.ui.widgetGallery.demoSuits.instruction;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.instruction.explanation.ExplanationDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.explanatory.ExplanatoryDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.note.NoteCompositeDemoSuite;

@DemoSuite({ExplanationDemoSuite.class, NoteCompositeDemoSuite.class, ExplanatoryDemoSuite.class})
@Demo
public class InstructionDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
