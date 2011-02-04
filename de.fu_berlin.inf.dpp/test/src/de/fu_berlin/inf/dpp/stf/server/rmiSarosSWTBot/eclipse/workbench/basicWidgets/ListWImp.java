package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class ListWImp extends EclipseComponentImp implements ListW {

    private static transient ListWImp listImp;

    /**
     * {@link ListWImp} is a singleton, but inheritance is possible.
     */
    public static ListWImp getInstance() {
        if (listImp != null)
            return listImp;
        listImp = new ListWImp();
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
