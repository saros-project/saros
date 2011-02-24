package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.hamcrest.Matcher;

public interface STFBotToolbarDropDownButton extends Remote {

    public List<STFBotMenu> menuItems(Matcher<MenuItem> matcher)
        throws RemoteException;

    public STFBotMenuImp menuItem(String menuItem) throws RemoteException;

    public STFBotMenuImp menuItem(Matcher<MenuItem> matcher)
        throws RemoteException;

    public void pressShortcut(KeyStroke... keys) throws RemoteException;
}
