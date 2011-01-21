package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Table;

public class ExistsTableItem extends DefaultCondition {

    private Table table;
    private String itemText;

    ExistsTableItem(Table table, String itemText) {
        this.table = table;
        this.itemText = itemText;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return table.existsTableItem(itemText);
    }
}
