package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.BasicWidgets;

public class ExistContextMenuOfTableItem extends DefaultCondition {

    private BasicWidgets basic;
    private String itemText;
    private String contextName;

    ExistContextMenuOfTableItem(BasicWidgets basic, String itemText,
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
