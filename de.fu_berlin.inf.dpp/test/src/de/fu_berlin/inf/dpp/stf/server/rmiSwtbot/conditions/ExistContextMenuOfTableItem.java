package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;

public class ExistContextMenuOfTableItem extends DefaultCondition {

    private BasicComponent basic;
    private String itemText;
    private String contextName;

    ExistContextMenuOfTableItem(BasicComponent basic, String itemText,
        String contextName) {
        this.basic = basic;
        this.itemText = itemText;
        this.contextName = contextName;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return basic.isContextMenuOfTableEnabled(itemText, contextName);
    }
}
