package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

public class RemoteBotCombo extends AbstractRmoteWidget implements IRemoteBotCombo {

    private static transient RemoteBotCombo self;

    private SWTBotCombo widget;

    /**
     * {@link RemoteBotCombo} is a singleton, but inheritance is possible.
     */
    public static RemoteBotCombo getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotCombo();
        return self;
    }

    public IRemoteBotCombo setWidget(SWTBotCombo ccomb) {
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

    /**
     * @see SWTBotCombo#contextMenu(String)
     */
    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    /**
     * @see SWTBotCombo#typeText(String)
     */
    public void typeText(String text) throws RemoteException {
        widget.typeText(text);

    }

    /**
     * @see SWTBotCombo#typeText(String, int)
     */
    public void typeText(String text, int interval) throws RemoteException {
        widget.typeText(text, interval);
    }

    /**
     * @see SWTBotCombo#setFocus()
     */
    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**
     * @see SWTBotCombo#setText(String)
     */
    public void setText(String text) throws RemoteException {
        widget.setText(text);
    }

    /**
     * @see SWTBotCombo#setSelection(String)
     */
    public void setSelection(String text) throws RemoteException {
        widget.setSelection(text);
    }

    /**
     * @see SWTBotCombo#setSelection(int)
     */
    public void setSelection(int index) throws RemoteException {
        widget.setSelection(index);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    /**
     * @see SWTBotCombo#itemCount()
     */
    public int itemCount() throws RemoteException {
        return widget.itemCount();
    }

    /**
     * @see SWTBotCombo#items()
     */
    public String[] items() throws RemoteException {
        return widget.items();
    }

    /**
     * @see SWTBotCombo#selection()
     */
    public String selection() throws RemoteException {
        return widget.selection();
    }

    /**
     * @see SWTBotCombo#selectionIndex()
     */
    public int selectionIndex() throws RemoteException {
        return widget.selectionIndex();
    }

    /**
     * @see SWTBotCombo#isEnabled()
     */
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    /**
     * @see SWTBotCombo#isVisible()
     */
    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    /**
     * @see SWTBotCombo#isActive()
     */
    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    /**
     * @see SWTBotCombo#getText()
     */
    public String getText() throws RemoteException {
        return widget.getText();
    }

    /**
     * @see SWTBotCombo#getToolTipText()
     */
    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

}
