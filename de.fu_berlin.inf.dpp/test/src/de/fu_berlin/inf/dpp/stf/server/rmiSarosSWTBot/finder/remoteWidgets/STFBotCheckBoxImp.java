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
    public STFBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void click() throws RemoteException {
        widget.click();
    }

    public void select() throws RemoteException {
        widget.select();
    }

    public void deselect() throws RemoteException {
        widget.deselect();

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

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

}
