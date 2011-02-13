package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotListImp extends EclipseComponentImp implements STFBotList {

    private static transient STFBotListImp listImp;

    /**
     * {@link STFBotListImp} is a singleton, but inheritance is possible.
     */
    public static STFBotListImp getInstance() {
        if (listImp != null)
            return listImp;
        listImp = new STFBotListImp();
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
