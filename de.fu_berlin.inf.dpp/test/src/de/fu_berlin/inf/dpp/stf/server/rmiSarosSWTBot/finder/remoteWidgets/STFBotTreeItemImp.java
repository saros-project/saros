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

    private SWTBotTreeItem widget;

    private SWTBotTree swtBotTree;

    /**
     * {@link STFBotTreeItemImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTreeItemImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotTreeItemImp();
        return self;
    }

    public STFBotTreeItem setWidget(SWTBotTreeItem item) {

        this.widget = item;
        return this;
    }

    public void setSWTBotTree(SWTBotTree tree) {
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
        stfBotMenu.setWidget(widget.contextMenu(text));
        return stfBotMenu;
    }

    public STFBotMenu contextMenu(String... texts) throws RemoteException {
        stfBotMenu.setWidget(ContextMenuHelper
            .getContextMenu(swtBotTree, texts));
        return stfBotMenu;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void toggleCheck() throws RemoteException {
        widget.toggleCheck();
    }

    public void uncheck() throws RemoteException {
        widget.uncheck();
    }

    public STFBotTreeItem select(String... items) throws RemoteException {
        return setWidget(widget.select(items));
    }

    public STFBotTreeItem select() throws RemoteException {
        return setWidget(widget.select());
    }

    public STFBotTreeItem doubleClick() throws RemoteException {
        return setWidget(widget.doubleClick());
    }

    public STFBotTreeItem expand() throws RemoteException {
        return setWidget(widget.expand());
    }

    public STFBotTreeItem expandNode(String... nodes) throws RemoteException {
        return setWidget(widget.expandNode(nodes));
    }

    public void check() throws RemoteException {
        widget.check();
    }

    public STFBotTreeItem collapse() throws RemoteException {
        return setWidget(widget.collapse());
    }

    public STFBotTreeItem collapseNode(String nodeText) throws RemoteException {
        return setWidget(widget.collapseNode(nodeText));
    }

    public STFBotTreeItem select(String item) throws RemoteException {
        return setWidget(widget.select(item));
    }

    public void click() throws RemoteException {
        widget.click();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isSelected() throws RemoteException {
        return widget.isSelected();
    }

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    public boolean isExpanded() throws RemoteException {
        return widget.isExpanded();
    }

    public int rowCount() throws RemoteException {
        return widget.rowCount();
    }

    public STFBotTreeItem getNode(int row) throws RemoteException {
        return setWidget(widget.getNode(row));
    }

    public STFBotTreeItem getNode(String nodeText) throws RemoteException {
        return setWidget(widget.getNode(nodeText));
    }

    public STFBotTreeItem getNode(String nodeText, int index)
        throws RemoteException {
        return setWidget(widget.getNode(nodeText, index));
    }

    public List<String> getNodes() throws RemoteException {
        return widget.getNodes();
    }

    public List<STFBotTreeItem> getNodes(String nodeText)
        throws RemoteException {
        List<STFBotTreeItem> items = new ArrayList<STFBotTreeItem>();
        for (SWTBotTreeItem item : widget.getNodes(nodeText)) {
            items.add(setWidget(item));
        }
        return items;
    }

    public List<String> getTextOfItems() throws RemoteException {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : widget.getItems()) {
            allItemTexts.add(item.getText());
            log.info("existed subTreeItem of the TreeItem " + widget.getText()
                + ": " + item.getText());
        }
        return allItemTexts;
    }

    // public STFBotTreeItem[] getItems() throws RemoteException {
    // subItems = widget.getItems();
    // STFBotTreeItem[] items = new STFBotTreeItem[widget.getItems().length];
    // for (int i = 0; i < widget.getItems().length; i++) {
    // items[i] = setWidget(widget.getItems()[i]);
    // }
    // return items;
    // }

    public boolean existsSubItem(String text) throws RemoteException {
        return getTextOfItems().contains(text);
    }

    public boolean existsSubItemWithRegex(String regex) throws RemoteException {
        for (String itemText : getTextOfItems()) {
            if (itemText.matches(regex))
                return true;
        }
        return false;
    }

    public boolean isContextMenuEnabled(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.isContextMenuEnabled(swtBotTree, contextNames);
    }

    public boolean existsContextMenu(String... contextNames)
        throws RemoteException {
        return ContextMenuHelper.existsContextMenu(swtBotTree, contextNames);
    }

    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    /**********************************************
     * 
     * wait until
     * 
     **********************************************/

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

}
