package de.fu_berlin.inf.dpp.test.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.test.stubs.IncomingInvitationProcessStub;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;

public class TestJoinWizardAction extends Action {

    public TestJoinWizardAction() {
        setText("test join wizard");
    }
    
    @Override
    public void run() {
        try {
            Shell shell = Display.getDefault().getActiveShell();
            IncomingInvitationProcessStub processStub = new IncomingInvitationProcessStub(5);
            new WizardDialog(shell, new JoinSessionWizard(processStub)).open();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
}
