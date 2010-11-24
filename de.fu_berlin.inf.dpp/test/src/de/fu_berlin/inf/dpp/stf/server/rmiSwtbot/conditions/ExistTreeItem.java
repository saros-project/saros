package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class ExistTreeItem extends DefaultCondition {

    private SWTBotTreeItem item;
    private String nodeName;

    ExistTreeItem(SWTBotTreeItem treeItem, String nodeName) {
        this.item = treeItem;
        this.nodeName = nodeName;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            item.getNode(nodeName);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
