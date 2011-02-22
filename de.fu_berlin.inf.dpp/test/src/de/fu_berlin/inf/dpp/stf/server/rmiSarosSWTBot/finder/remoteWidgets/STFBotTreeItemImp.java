package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class STFBotTreeItemImp extends AbstractRmoteWidget implements
    STFBotTreeItem {
    private static transient STFBotTreeItemImp self;

    private SWTBotTreeItem swtBotTreeItem;

    private SWTBotTree swtBotTree;

    private static STFBotMenuImp menu;

    public SWTBotTreeItem getSwtBotTreeItem() throws RemoteException {
        return swtBotTreeItem;
    }

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTreeItemImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotTreeItemImp();
        menu = STFBotMenuImp.getInstance();
        return self;
    }

    public void setSWTBotTreeItem(SWTBotTreeItem item) throws RemoteException {
        this.swtBotTreeItem = item;
    }

    public void setSWTBotTree(SWTBotTree tree) throws RemoteException {
        this.swtBotTree = tree;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finder
     * 
     **********************************************/

    public STFBotMenu contextMenu(String text) throws RemoteException {
        menu.setWidget(swtBotTreeItem.contextMenu(text));
        return menu;
    }

    public STFBotMenu contextMenu(String... texts) throws RemoteException {
        menu.setWidget(ContextMenuHelper.getContextMenu(swtBotTree, texts));
        return menu;
    }

    public List<String> getSubItems() throws RemoteException {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : swtBotTreeItem.getItems()) {
            allItemTexts.add(item.getText());
            log.info("existed subTreeItem of the TreeItem "
                + swtBotTreeItem.getText() + ": " + item.getText());
        }
        return allItemTexts;
    }

    public boolean existsSubItem(String text) throws RemoteException {
        return getSubItems().contains(text);
    }

    public boolean existsSubItemWithRegex(String regex) throws RemoteException {
        for (String itemText : getSubItems()) {
            if (itemText.matches(regex))
                return true;
        }
        return false;
    }

    public void waitUntilSubItemExists(final String subItemText)
        throws RemoteException {
        stfBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsSubItem(subItemText);
            }

            public String getFailureMessage() {
                return "The tree node" + "doesn't contain the treeItem"
                    + subItemText;
            }
        });
    }

    public boolean isContextMenuEnabled(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.isContextMenuEnabled(swtBotTree, contextNames);
    }

    public boolean existsContextMenu(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.existsContextMenu(swtBotTree, contextNames);
    }

}
