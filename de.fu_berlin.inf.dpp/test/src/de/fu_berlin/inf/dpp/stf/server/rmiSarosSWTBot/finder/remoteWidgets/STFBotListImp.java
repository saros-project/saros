package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;

public class STFBotListImp extends AbstractRmoteWidget implements STFBotList {

    private static transient STFBotListImp listImp;

    private SWTBotList list;

    /**
     * {@link STFBotListImp} is a singleton, but inheritance is possible.
     */
    public static STFBotListImp getInstance() {
        if (listImp != null)
            return listImp;
        listImp = new STFBotListImp();
        return listImp;
    }

    public void setWidget(SWTBotList list) {
        this.list = list;
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

    public String itemAt(int index) throws RemoteException {
        return list.itemAt(index);
    }

    public int itemCount() throws RemoteException {
        return list.itemCount();
    }

    public int indexOf(String item) throws RemoteException {
        return list.indexOf(item);
    }

    public void select(String item) throws RemoteException {
        list.select(item);
    }

    public void select(int... indices) throws RemoteException {
        list.select(indices);
    }

    public void select(int index) throws RemoteException {
        list.select(index);
    }

    public void select(String... items) throws RemoteException {
        list.select(items);
    }

    public void selectionCount() throws RemoteException {
        list.selectionCount();
    }

    public void unselect() throws RemoteException {
        list.unselect();
    }

    public void setFocus() throws RemoteException {
        list.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String[] getItems() throws RemoteException {
        return list.getItems();
    }

    public boolean isEnabled() throws RemoteException {
        return list.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return list.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return list.isActive();
    }

    public String getText() throws RemoteException {
        return list.getText();
    }

    public String getToolTipText() throws RemoteException {
        return list.getText();
    }

}
