package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface STFBotTreeItem extends Remote {
    /**********************************************
     * 
     * finder
     * 
     **********************************************/

    public STFBotMenu contextMenu(String text) throws RemoteException;

    public STFBotMenu contextMenu(String... texts) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void toggleCheck() throws RemoteException;

    public void uncheck() throws RemoteException;

    public STFBotTreeItem select(String... items) throws RemoteException;

    public STFBotTreeItem select() throws RemoteException;

    public STFBotTreeItem doubleClick() throws RemoteException;

    public STFBotTreeItem expand() throws RemoteException;

    public STFBotTreeItem expandNode(String... nodes) throws RemoteException;

    public void check() throws RemoteException;

    public STFBotTreeItem collapse() throws RemoteException;

    public STFBotTreeItem collapseNode(String nodeText) throws RemoteException;

    public STFBotTreeItem select(String item) throws RemoteException;

    public void click() throws RemoteException;

    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isSelected() throws RemoteException;

    public boolean isChecked() throws RemoteException;

    public boolean isExpanded() throws RemoteException;

    public int rowCount() throws RemoteException;

    public STFBotTreeItem getNode(int row) throws RemoteException;

    public STFBotTreeItem getNode(String nodeText) throws RemoteException;

    public STFBotTreeItem getNode(String nodeText, int index)
        throws RemoteException;

    public List<String> getNodes() throws RemoteException;

    public List<STFBotTreeItem> getNodes(String nodeText)
        throws RemoteException;

    public List<String> getTextOfItems() throws RemoteException;

    // public STFBotTreeItem[] getItems() throws RemoteException;

    public boolean existsSubItem(String text) throws RemoteException;

    public boolean existsSubItemWithRegex(String regex) throws RemoteException;

    public boolean isContextMenuEnabled(String... contextNames)
        throws RemoteException;

    public boolean existsContextMenu(String... contextNames)
        throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getText() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    /**********************************************
     * 
     * wait until
     * 
     **********************************************/

    public void waitUntilSubItemExists(final String subItemText)
        throws RemoteException;
}
