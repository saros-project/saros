package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public class RemoteBotToolbarButtonImp extends AbstractRmoteWidget implements
    RemoteBotToolbarButton {

    private static transient RemoteBotToolbarButtonImp ToolbarButtonImp;
    private SWTBotToolbarButton toolbarButton;

    /**
     * {@link RemoteBotToolbarButtonImp} is a singleton, but inheritance is
     * possible.
     */
    public static RemoteBotToolbarButtonImp getInstance() {
        if (ToolbarButtonImp != null)
            return ToolbarButtonImp;
        ToolbarButtonImp = new RemoteBotToolbarButtonImp();
        return ToolbarButtonImp;
    }

    public RemoteBotToolbarButton setWidget(SWTBotToolbarButton toolbarButton) {
        this.toolbarButton = toolbarButton;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(toolbarButton.contextMenu(text));

    }

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
        stfBot.waitUntil(Conditions.widgetIsEnabled(toolbarButton));
    }

}
