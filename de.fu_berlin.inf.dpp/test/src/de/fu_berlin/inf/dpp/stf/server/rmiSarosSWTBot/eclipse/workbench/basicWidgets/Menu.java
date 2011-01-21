package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Menu extends Remote {

    /**
     * clicks the main menus with the passed texts.
     * 
     * @param texts
     *            title of the menus, example: Window -> Show View -> Other...
     * 
     * @throws RemoteException
     */
    public void clickMenuWithTexts(String... texts) throws RemoteException;
}
