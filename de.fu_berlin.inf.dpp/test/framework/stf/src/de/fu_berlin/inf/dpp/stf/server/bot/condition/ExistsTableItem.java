package de.fu_berlin.inf.dpp.stf.server.bot.condition;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class ExistsTableItem extends DefaultCondition {

  private IRemoteBotTable table;
  private String itemText;

  ExistsTableItem(IRemoteBotTable table, String itemText) {
    this.table = table;
    this.itemText = itemText;
  }

  @Override
  public String getFailureMessage() {

    return null;
  }

  @Override
  public boolean test() throws Exception {
    return table.containsItem(itemText);
  }
}
