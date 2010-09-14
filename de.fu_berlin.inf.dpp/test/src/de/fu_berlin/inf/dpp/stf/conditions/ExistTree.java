package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

public class ExistTree extends DefaultCondition {

    private SWTBotTree tree;
    private String nodeText;

    ExistTree(SWTBotTree tree, String nodeText) {
        this.tree = tree;
        this.nodeText = nodeText;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            tree.getTreeItem(nodeText);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
