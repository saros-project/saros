package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;

public class RemoteBotCCombo extends AbstractRmoteWidget implements
    IRemoteBotCCombo {

    private static transient RemoteBotCCombo self;

    private SWTBotCCombo widget;

    /**
     * {@link RemoteBotCCombo} is a singleton, but inheritance is possible.
     */
    public static RemoteBotCCombo getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotCCombo();
        return self;
    }

    public IRemoteBotCCombo setWidget(SWTBotCCombo ccomb) {
        this.widget = ccomb;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void setSelection(int indexOfSelection) throws RemoteException {
        widget.setSelection(indexOfSelection);
    }

    public String selection() throws RemoteException {
        return widget.selection();
    }

    public int selectionIndex() throws RemoteException {
        return widget.selectionIndex();
    }

    public void setSelection(String text) throws RemoteException {
        widget.setSelection(text);
    }

    public void setText(String text) throws RemoteException {
        widget.setText(text);
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public int itemCount() throws RemoteException {
        return widget.itemCount();
    }

    public String[] items() throws RemoteException {
        return widget.items();
    }

    public int textLimit() throws RemoteException {
        return widget.textLimit();
    }

}
