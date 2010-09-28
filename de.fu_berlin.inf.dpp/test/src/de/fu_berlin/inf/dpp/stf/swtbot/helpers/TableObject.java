package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class TableObject {
    private static final transient Logger log = Logger
        .getLogger(TableObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public TableObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;

    }

    public SWTBotTableItem selectTableItemWithLabel(SWTBotTable table,
        String label) {
        try {
            wUntil.waitUntilTableItemExisted(table, label);
            return table.getTableItem(label);

        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }
}
