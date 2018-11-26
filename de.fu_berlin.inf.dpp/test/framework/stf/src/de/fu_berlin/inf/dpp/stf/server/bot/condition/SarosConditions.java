package de.fu_berlin.inf.dpp.stf.server.bot.condition;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.Chatroom;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/** This is a factory class to create some conditions provided with STF. */
public class SarosConditions extends Conditions {

  public static ICondition isShellActive(SWTBotShell shell) {
    return new IsShellActive(shell);
  }

  public static ICondition isShellOpen(SWTWorkbenchBot bot, String title) {
    return new IsShellOpen(bot, title);
  }

  public static ICondition existTableItem(IRemoteBotTable table, String tableItemName) {
    return new ExistsTableItem(table, tableItemName);
  }

  public static ICondition isShellClosed(SWTBotShell shell) {
    return new IsShellClosed(shell);
  }

  public static ICondition isResourceExist(String resourcePath) {
    return new ExistsResource(resourcePath);
  }

  public static ICondition isResourceNotExist(String resourcePath) {
    return new ExistsNoResource(resourcePath);
  }

  public static ICondition isChatMessageExist(Chatroom chatroom, String jid, String message) {
    return new ExistsChatMessage(chatroom, jid, message);
  }
}
