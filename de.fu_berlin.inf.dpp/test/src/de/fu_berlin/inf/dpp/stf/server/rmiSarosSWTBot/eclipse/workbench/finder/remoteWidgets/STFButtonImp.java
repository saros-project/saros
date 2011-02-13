package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFButtonImp extends EclipseComponentImp implements STFButton {

    private static transient STFButtonImp buttonImp;

    private SWTBotButton swtBotButton;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFButtonImp getInstance() {
        if (buttonImp != null)
            return buttonImp;
        buttonImp = new STFButtonImp();
        return buttonImp;
    }

    public void setSwtBotButton(SWTBotButton button) {
        this.swtBotButton = button;
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
    public void click() throws RemoteException {
        swtBotButton.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void selectCComboBox(int indexOfCComboBox, int indexOfSelection)
        throws RemoteException {
        bot.ccomboBox(indexOfCComboBox).setSelection(indexOfSelection);
    }

    public void clickCheckBox(String mnemonicText) throws RemoteException {
        bot.checkBox(mnemonicText).click();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return swtBotButton.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotButton.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotButton.isActive();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(swtBotButton));
    }

}
