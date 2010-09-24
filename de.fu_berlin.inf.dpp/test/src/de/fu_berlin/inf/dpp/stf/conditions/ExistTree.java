package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class ExistTree extends DefaultCondition {

    private SWTBotTree tree;
    private String[] nodeTexts;

    ExistTree(SWTBotTree tree, String... nodeTexts) {
        this.tree = tree;
        this.nodeTexts = nodeTexts;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            SWTBotTreeItem item = null;
            for (int i = 0; i < nodeTexts.length; i++) {
                if (item == null)
                    item = tree.getTreeItem(nodeTexts[i]);
                else {
                    item = item.getNode(nodeTexts[i]);
                }
            }
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
