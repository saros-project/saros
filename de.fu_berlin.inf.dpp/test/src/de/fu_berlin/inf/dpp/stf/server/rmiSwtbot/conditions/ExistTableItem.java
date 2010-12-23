package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;

public class ExistTableItem extends DefaultCondition {

    private BasicComponent basic;
    private String itemText;

    ExistTableItem(BasicComponent basic, String itemText) {
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
