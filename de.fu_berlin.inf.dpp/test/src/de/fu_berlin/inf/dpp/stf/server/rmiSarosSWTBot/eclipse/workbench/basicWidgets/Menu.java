package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponent;

public interface Menu extends EclipseComponent {

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
