package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTable;

public class ExistsTableItem extends DefaultCondition {

    private IRemoteBotTable table;
    private String itemText;

    ExistsTableItem(IRemoteBotTable table, String itemText) {
        this.table = table;
        this.itemText = itemText;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return table.containsItem(itemText);
    }
}
