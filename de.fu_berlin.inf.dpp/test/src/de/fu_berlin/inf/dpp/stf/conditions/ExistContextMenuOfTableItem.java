package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

public class ExistContextMenuOfTableItem extends DefaultCondition {

    private SWTBotTableItem tableItem;
    private String text;

    ExistContextMenuOfTableItem(SWTBotTableItem tableItem, String text) {
        this.tableItem = tableItem;
        this.text = text;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            tableItem.contextMenu(text);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
