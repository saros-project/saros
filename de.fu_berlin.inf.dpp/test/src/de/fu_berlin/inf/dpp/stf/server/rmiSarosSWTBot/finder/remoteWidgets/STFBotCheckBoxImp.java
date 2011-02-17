package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

public class STFBotCheckBoxImp extends AbstractRmoteWidget implements
    STFBotCheckBox {

    private static transient STFBotCheckBoxImp self;

    private SWTBotCheckBox swtBotCheckBox;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotCheckBoxImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotCheckBoxImp();
        return self;
    }

    public void setSWTBotWidget(SWTBotCheckBox checkBox) {
        this.swtBotCheckBox = checkBox;
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
        swtBotCheckBox.click();
    }

    public void select() throws RemoteException {
        swtBotCheckBox.select();
    }

    public void deselect() throws RemoteException {
        swtBotCheckBox.deselect();

    }

    public void setFocus() throws RemoteException {
        swtBotCheckBox.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return swtBotCheckBox.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotCheckBox.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotCheckBox.isActive();
    }

    public boolean isChecked() throws RemoteException {
        return swtBotCheckBox.isChecked();
    }

    public String getText() throws RemoteException {
        return swtBotCheckBox.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotCheckBox.getText();
    }

}
