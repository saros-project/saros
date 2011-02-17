package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public class STFBotToolbarButtonImp extends AbstractRmoteWidget implements
    STFBotToolbarButton {

    private static transient STFBotToolbarButtonImp ToolbarButtonImp;
    private SWTBotToolbarButton toolbarButton;

    /**
     * {@link STFBotToolbarButtonImp} is a singleton, but inheritance is
     * possible.
     */
    public static STFBotToolbarButtonImp getInstance() {
        if (ToolbarButtonImp != null)
            return ToolbarButtonImp;
        ToolbarButtonImp = new STFBotToolbarButtonImp();
        return ToolbarButtonImp;
    }

    public void setSwtBotToolbarButton(SWTBotToolbarButton toolbarButton) {
        this.toolbarButton = toolbarButton;
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
        toolbarButton.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        toolbarButton.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean isEnabled() throws RemoteException {
        return toolbarButton.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return toolbarButton.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return toolbarButton.isActive();
    }

    public String getText() throws RemoteException {
        return toolbarButton.getText();
    }

    public String getToolTipText() throws RemoteException {
        return toolbarButton.getToolTipText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        bot.waitUntil(Conditions.widgetIsEnabled(toolbarButton));
    }

}
