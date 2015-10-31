package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.ExplanationDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.ExplanatoryDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.note.NoteCompositeDemoSuite;

@DemoSuite({ ExplanationDemoSuite.class, NoteCompositeDemoSuite.class,
    ExplanatoryDemoSuite.class })
@Demo
public class InstructionDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }
}
