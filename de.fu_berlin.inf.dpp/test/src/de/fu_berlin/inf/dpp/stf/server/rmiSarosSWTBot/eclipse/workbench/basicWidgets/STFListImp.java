package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class STFListImp extends EclipseComponentImp implements STFList {

    private static transient STFListImp listImp;

    /**
     * {@link STFListImp} is a singleton, but inheritance is possible.
     */
    public static STFListImp getInstance() {
        if (listImp != null)
            return listImp;
        listImp = new STFListImp();
        return listImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String[] getListItemsInGroup(String inGroup) throws RemoteException {
        return bot.listInGroup(inGroup).getItems();
    }

    public void selectListItemInGroup(String item, String inGroup)
        throws RemoteException {
        bot.listInGroup(inGroup).select(item);

    }
}
