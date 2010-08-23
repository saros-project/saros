package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

public class SarosConditions extends Conditions {

    /**
     * Gets the condition for checking if the connection successfully connectet
     * 
     * @param shellText
     *            the text of the shell.
     * @return a condition that evaluates to false until the connection
     *         connectet.
     * @since 1.3
     */
    public static ICondition isConnect(SWTWorkbenchBot bot) {
        return new IsConnect(bot);
    }
}
