package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

public class ExistTableItem extends DefaultCondition {

    private SWTBotTable table;
    private String tableItemName;

    ExistTableItem(SWTBotTable table, String tableItemName) {
        this.table = table;
        this.tableItemName = tableItemName;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            table.getTableItem(tableItemName);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
