package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;

public class STFBotCComboImp extends AbstractRmoteWidget implements
    STFBotCCombo {

    private static transient STFBotCComboImp self;

    private SWTBotCCombo swtBotCCombo;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotCComboImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotCComboImp();
        return self;
    }

    public void setSwtBotCCombo(SWTBotCCombo ccomb) {
        this.swtBotCCombo = ccomb;
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

    public void setSelection(int indexOfSelection) throws RemoteException {
        swtBotCCombo.setSelection(indexOfSelection);
    }

    public void selection() throws RemoteException {
        swtBotCCombo.selection();
    }

    public void selectionIndex() throws RemoteException {
        swtBotCCombo.selectionIndex();
    }

    public void setSelection(String text) throws RemoteException {
        swtBotCCombo.setSelection(text);
    }

    public void setText(String text) throws RemoteException {
        swtBotCCombo.setText(text);
    }

    public void setFocus() throws RemoteException {
        swtBotCCombo.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return swtBotCCombo.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotCCombo.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotCCombo.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotCCombo.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotCCombo.getText();
    }

    public int itemCount() throws RemoteException {
        return swtBotCCombo.itemCount();
    }

    public String[] items() throws RemoteException {
        return swtBotCCombo.items();
    }

    public int textLimit() throws RemoteException {
        return swtBotCCombo.textLimit();
    }

}
