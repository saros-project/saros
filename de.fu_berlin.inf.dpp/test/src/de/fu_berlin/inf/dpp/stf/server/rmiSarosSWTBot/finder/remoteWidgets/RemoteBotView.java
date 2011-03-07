package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteBot;

public interface RemoteBotView extends Remote {

    public void waitUntilIsActive() throws RemoteException;

    public void show() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public void close() throws RemoteException;

    public void setFocus() throws RemoteException;

    public RemoteBot bot() throws RemoteException;

    public RemoteBotViewMenu menu(String label) throws RemoteException;

    public RemoteBotViewMenu menu(String label, int index) throws RemoteException;

    public RemoteBotToolbarButton toolbarButton(String tooltip)
        throws RemoteException;

    public boolean existsToolbarButton(String tooltip) throws RemoteException;

    public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException;

    public RemoteBotToolbarButton toolbarButtonWithRegex(String regex)
        throws RemoteException;

    public RemoteBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
        throws RemoteException;

    public RemoteBotToolbarRadioButton toolbarRadioButton(String tooltip)
        throws RemoteException;

    public RemoteBotToolbarPushButton toolbarPushButton(String tooltip)
        throws RemoteException;

    public RemoteBotToolbarToggleButton toolbarToggleButton(String tooltip)
        throws RemoteException;

    public List<RemoteBotViewMenu> menus() throws RemoteException;

    public List<RemoteBotToolbarButton> getToolbarButtons() throws RemoteException;

    public String getTitle() throws RemoteException;

    public List<String> getToolTipTextOfToolbarButtons() throws RemoteException;

}
