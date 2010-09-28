package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class ToolbarObject {
    private RmiSWTWorkbenchBot rmiBot;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public ToolbarObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

    public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {
        // return
        // delegate.viewByTitle(title).toolbarButton(buttonTooltip).click();
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton.click();
            }
        }
        return null;

        // throw new RemoteException("Button with tooltip '" + buttonTooltip
        // + "' was not found on view with title '" + title + "'");
    }

    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) {
        bot.viewByTitle(viewName).toolbarPushButton(tooltip).click();
    }

}
