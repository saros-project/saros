package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public interface STFBotTreeItem extends Remote {
    public void setSWTBotTreeItem(SWTBotTreeItem item) throws RemoteException;

    public void setSWTBotTree(SWTBotTree tree) throws RemoteException;

    public STFBotMenu contextMenu(String text) throws RemoteException;

    public STFBotMenu contextMenu(String... texts) throws RemoteException;

    public List<String> getSubItems() throws RemoteException;

    public boolean existsSubItem(String text) throws RemoteException;

    public boolean existsSubItemWithRegex(String regex) throws RemoteException;

    public void waitUntilSubItemExists(String itemText) throws RemoteException;

    public boolean isContextMenuEnabled(String... contextNames)
        throws RemoteException;

    public boolean existsContextMenu(String... contextNames)
        throws RemoteException;

    public SWTBotTreeItem getSwtBotTreeItem() throws RemoteException;
}
