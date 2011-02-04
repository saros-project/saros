package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class ButtonImp extends EclipseComponentImp implements Button {

    private static transient ButtonImp buttonImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static ButtonImp getInstance() {
        if (buttonImp != null)
            return buttonImp;
        buttonImp = new ButtonImp();
        return buttonImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void clickButton(String mnemonicText) throws RemoteException {
        bot.button(mnemonicText).click();
    }

    public void clickButtonAndWait(String mnemonicText) throws RemoteException {
        waitUntilButtonEnabled(mnemonicText);
        clickButton(mnemonicText);
    }

    public void clickButtonWithTooltip(String tooltip) throws RemoteException {
        bot.buttonWithTooltip(tooltip).click();
    }

    public void clickButtonInGroup(String groupTitle) throws RemoteException {
        bot.buttonInGroup(groupTitle).click();
    }

    public void clickButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        bot.buttonInGroup(mnemonicText, inGroup).click();
    }

    public void selectCComboBox(int indexOfCComboBox, int indexOfSelection) throws RemoteException {
        bot.ccomboBox(indexOfCComboBox).setSelection(indexOfSelection);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException {
        return bot.button(mnemonicText).isEnabled();
    }

    public boolean isButtonWithTooltipEnabled(String tooltip)
        throws RemoteException {
        return bot.buttonWithTooltip(tooltip).isEnabled();
    }

    public boolean existsButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        try {
            bot.buttonInGroup(mnemonicText, inGroup);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    public void waitUnitButtonWithTooltipIsEnabled(String tooltip)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.buttonWithTooltip(tooltip)));
    }

}
