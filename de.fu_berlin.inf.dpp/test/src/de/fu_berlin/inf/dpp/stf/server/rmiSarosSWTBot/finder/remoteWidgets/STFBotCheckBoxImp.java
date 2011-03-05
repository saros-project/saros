package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

public class STFBotCheckBoxImp extends AbstractRmoteWidget implements
    STFBotCheckBox {

    private static transient STFBotCheckBoxImp self;

    private SWTBotCheckBox widget;

    /**
     * {@link STFBotCheckBoxImp} is a singleton, but inheritance is possible.
     */
    public static STFBotCheckBoxImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotCheckBoxImp();
        return self;
    }

    public STFBotCheckBox setWidget(SWTBotCheckBox checkBox) {
        this.widget = checkBox;
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
     * 
     * @see SWTBotCheckBox#contextMenu(String)
     */
    public STFBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    /**
     * 
     * @see SWTBotCheckBox#click()
     */
    public void click() throws RemoteException {
        widget.click();
    }

    /**
     * 
     * @see SWTBotCheckBox#select()
     */
    public void select() throws RemoteException {
        widget.select();
    }

    /**
     * 
     * @see SWTBotCheckBox#deselect()
     */
    public void deselect() throws RemoteException {
        widget.deselect();

    }

    /**
     * 
     * @see SWTBotCheckBox#setFocus()
     */
    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    /**
     * 
     * @see SWTBotCheckBox#isEnabled()
     */
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    /**
     * 
     * @see SWTBotCheckBox#isVisible()
     */
    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    /**
     * 
     * @see SWTBotCheckBox#isActive()
     */
    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    /**
     * 
     * @see SWTBotCheckBox#isChecked()
     */
    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    /**
     * 
     * @see SWTBotCheckBox#getText()
     */
    public String getText() throws RemoteException {
        return widget.getText();
    }

    /**
     * 
     * @see SWTBotCheckBox#getToolTipText()
     */
    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

}
