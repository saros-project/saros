package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponent;

public interface STFMenu extends EclipseComponent {

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

    public STFMenuImp contextMenu(String text) throws RemoteException;

    public void setWidget(SWTBotMenu widget) throws RemoteException;
}
