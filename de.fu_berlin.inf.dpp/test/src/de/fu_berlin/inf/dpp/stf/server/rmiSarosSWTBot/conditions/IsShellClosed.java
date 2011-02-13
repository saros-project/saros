package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellClosed extends DefaultCondition {

    private SWTBotShell shell;

    IsShellClosed(SWTBotShell shell) {

        this.shell = shell;
    }

    public String getFailureMessage() {

        return "STFBotShell is still open.";
    }

    public boolean test() throws Exception {
        return !shell.isOpen();
    }
}