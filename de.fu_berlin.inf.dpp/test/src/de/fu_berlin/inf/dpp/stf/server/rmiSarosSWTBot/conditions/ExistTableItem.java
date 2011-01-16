package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.BasicWidgets;

public class ExistTableItem extends DefaultCondition {

    private BasicWidgets basic;
    private String itemText;

    ExistTableItem(BasicWidgets basic, String itemText) {
        this.basic = basic;
        this.itemText = itemText;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return basic.existsTableItem(itemText);
    }
}
