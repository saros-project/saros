package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellActive extends DefaultCondition {

    private SWTBotShell shell;

    IsShellActive(SWTBotShell shell) {
        this.shell = shell;
    }

    public String getFailureMessage() {
        return "STFBotShell  not found.";
    }

    public boolean test() throws Exception {
        return shell.isActive();
    }
}
