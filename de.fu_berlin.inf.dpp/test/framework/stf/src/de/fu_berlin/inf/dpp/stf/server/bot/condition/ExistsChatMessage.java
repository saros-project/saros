package de.fu_berlin.inf.dpp.stf.server.bot.condition;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.Chatroom;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class ExistsChatMessage extends DefaultCondition {

  private String jid;
  private String message;
  private Chatroom chatroom;

  ExistsChatMessage(Chatroom sarosView, String jid, String message) {
    this.chatroom = sarosView;
    this.jid = jid;
    this.message = message;
  }

  @Override
  public String getFailureMessage() {
    return null;
  }

  @Override
  public boolean test() throws Exception {
    return chatroom.compareChatMessage(jid, message);
  }
}
