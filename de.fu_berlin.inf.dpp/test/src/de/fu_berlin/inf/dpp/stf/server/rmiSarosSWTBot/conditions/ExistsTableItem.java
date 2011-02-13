package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;

public class ExistsTableItem extends DefaultCondition {

    private STFBotTable table;
    private String itemText;

    ExistsTableItem(STFBotTable table, String itemText) {
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
