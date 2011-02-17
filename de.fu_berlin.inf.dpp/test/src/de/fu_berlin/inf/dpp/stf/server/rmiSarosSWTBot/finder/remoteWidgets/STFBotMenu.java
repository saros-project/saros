package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface STFBotMenu extends EclipseComponent {

    /**
     * clicks the main menus with the passed texts.
     * 
     * @param texts
     *            title of the menus, example: Window -> Show View -> Other...
     * 
     * @throws RemoteException
     */
    public void clickMenuWithTexts(String... texts) throws RemoteException;

    public void click() throws RemoteException;

    public STFBotMenuImp contextMenu(String text) throws RemoteException;

    public void waitUntilIsEnabled() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    public String getText() throws RemoteException;

    public boolean isChecked() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public void setFocus() throws RemoteException;

    public STFBotMenu menu(String menuName) throws RemoteException;

}
