package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

/**
 * This class contains basic API to find widgets based on the workbench in
 * SWTBot and to perform the operations on it, which is only used by rmi server
 * side and not exported.
 * 
 * @author lchen
 */
public class BasicPart extends EclipseComponent {

    /**
     * Waits until the button is enabled.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void waitUntilButtonIsEnabled(String mnemonicText) {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    /**
     * Waits until the button is enabled.
     * 
     * @param tooltipText
     *            the tooltip on the widget.
     */
    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText) {
        waitUntil(Conditions
            .widgetIsEnabled(bot.buttonWithTooltip(tooltipText)));
    }

    public void setTextInTextWithLabel(String text, String label) {
        bot.textWithLabel(label).setText(text);
    }

    public void clickButton(String mnemonicText) {
        bot.button(mnemonicText).click();
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
