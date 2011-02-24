package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

public class STFBotComboImp extends AbstractRmoteWidget implements STFBotCombo {

    private static transient STFBotComboImp self;

    private SWTBotCombo swtBotCombo;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotComboImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotComboImp();
        return self;
    }

    public void setSwtBotCombo(SWTBotCombo ccomb) {
        this.swtBotCombo = ccomb;
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

    public void typeText(String text) throws RemoteException {
        swtBotCombo.typeText(text);

    }

    public void typeText(String text, int interval) throws RemoteException {
        swtBotCombo.typeText(text, interval);
    }

    public void setFocus() throws RemoteException {
        swtBotCombo.setFocus();
    }

    public void setText(String text) throws RemoteException {
        swtBotCombo.setText(text);
    }

    public void setSelection(String text) throws RemoteException {
        swtBotCombo.setSelection(text);
    }

    public void setSelection(int index) throws RemoteException {
        swtBotCombo.setSelection(index);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public int itemCount() throws RemoteException {
        return swtBotCombo.itemCount();
    }

    public String[] items() throws RemoteException {
        return swtBotCombo.items();
    }

    public String selection() throws RemoteException {
        return swtBotCombo.selection();
    }

    public int selectionIndex() throws RemoteException {
        return swtBotCombo.selectionIndex();
    }

    public boolean isEnabled() throws RemoteException {
        return swtBotCombo.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotCombo.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotCombo.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotCombo.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotCombo.getText();
    }

}
