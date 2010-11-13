package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import org.eclipse.swtbot.swt.finder.waits.Conditions;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosControler;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseObject;

public class BasicObject extends EclipseObject {

    public BasicObject(SarosControler rmiBot) {
        super(rmiBot);
    }

    public void waitUntilButtonEnabled(String mnemonicText) {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    public void waitUnitButtonWithTooltipTextEnabled(String tooltipText) {
        waitUntil(Conditions
            .widgetIsEnabled(bot.buttonWithTooltip(tooltipText)));
    }

}
