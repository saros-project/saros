package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;

public interface STFBotView extends Remote {

    public void waitUntilIsActive() throws RemoteException;

    public void show() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public void close() throws RemoteException;

    public void setFocus() throws RemoteException;

    public STFBot bot() throws RemoteException;

    public STFBotViewMenu menu(String label) throws RemoteException;

    public STFBotViewMenu menu(String label, int index) throws RemoteException;

    public STFBotToolbarButton toolbarButton(String tooltip)
        throws RemoteException;

    public boolean existsToolbarButton(String tooltip) throws RemoteException;

    public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException;

    public STFBotToolbarButton toolbarButtonWithRegex(String regex)
        throws RemoteException;

    public STFBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
        throws RemoteException;

    public STFBotToolbarRadioButton toolbarRadioButton(String tooltip)
        throws RemoteException;

    public STFBotToolbarPushButton toolbarPushButton(String tooltip)
        throws RemoteException;

    public STFBotToolbarToggleButton toolbarToggleButton(String tooltip)
        throws RemoteException;

    public List<STFBotViewMenu> menus() throws RemoteException;

    public List<STFBotToolbarButton> getToolbarButtons() throws RemoteException;

    public String getTitle() throws RemoteException;

    public List<String> getToolTipTextOfToolbarButtons() throws RemoteException;

}
