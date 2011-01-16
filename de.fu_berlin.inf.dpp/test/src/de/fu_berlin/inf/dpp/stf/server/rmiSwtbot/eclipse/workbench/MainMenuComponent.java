package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;

public interface MainMenuComponent extends Remote {

    /**************************************************************
     * 
     * Basic actions for main menu
     * 
     **************************************************************/
    /**
     * clicks the main menus with the passed texts.
     * 
     * @param texts
     *            title of the menus, example: Window -> Show View -> Other...
     * 
     * @throws RemoteException
     */
    // public void clickMenuWithTexts(String... texts) throws RemoteException;

}
