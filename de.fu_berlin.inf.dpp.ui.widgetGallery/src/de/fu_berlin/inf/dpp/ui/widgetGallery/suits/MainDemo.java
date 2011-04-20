package de.fu_berlin.inf.dpp.ui.widgetGallery.suits;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.BasicDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.ChatDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.decoration.DecorationDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.InstructionDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.project.ProjectDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.roster.RosterDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.rosterSession.RosterSessionDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.WizardDemoContainer;

public class MainDemo extends DemoContainer {

    public MainDemo(Composite parent) {
	super(parent);
    }

    public Control getControl() {
	return this.control;
    }

    @Override
    public void createPartControls(Composite parent) {
	super.createPartControls(parent);

	new DecorationDemoContainer(this, "Decoration");
	new BasicDemoContainer(this, "Composite");
	new InstructionDemoContainer(this, "Explanation");
	new ChatDemoContainer(this, "Chat");
	new RosterDemoContainer(this, "Roster");
	open(new RosterSessionDemoContainer(this, "RosterSession"));
	new ProjectDemoContainer(this, "Project");
	new WizardDemoContainer(this, "Wizard");
    }
}
