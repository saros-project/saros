package saros.stf.server.rmi.superbot.component.view.saros.impl;

import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.SarosSWTBot;
import saros.stf.server.bot.condition.SarosConditions;
import saros.stf.server.bot.widget.SarosSWTBotChatInput;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.component.view.saros.IChatroom;

public final class Chatroom extends StfRemoteObject implements IChatroom {

  private static final Logger log = Logger.getLogger(Chatroom.class);

  private static final Chatroom INSTANCE = new Chatroom();

  private SWTBotCTabItem chatTab;

  public static Chatroom getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean compareChatMessage(String jid, String message) throws RemoteException {
    try {
      chatTab.activate();
      SarosSWTBot bot = new SarosSWTBot(chatTab.widget);
      String text = bot.lastChatLine().getText();
      return text.equals(message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  public void setChatTab(SWTBotCTabItem chatTab) {
    this.chatTab = chatTab;
  }

  @Override
  public void enterChatMessage(String message) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    chatTab.activate();
    SarosSWTBotChatInput chatInput = new SarosSWTBot(chatTab.widget).chatInput();
    chatInput.typeText(message);
  }

  @Override
  public void clearChatMessage() throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    chatTab.activate();
    SarosSWTBotChatInput chatInput = new SarosSWTBot(chatTab.widget).chatInput();
    chatInput.setText("");
  }

  @Override
  public void sendChatMessage() throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    chatTab.activate();
    SarosSWTBotChatInput chatInput = new SarosSWTBot(chatTab.widget).chatInput();
    chatInput.pressEnterKey();
  }

  @Override
  public void sendChatMessage(String message) throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    chatTab.activate();
    SarosSWTBotChatInput chatInput = new SarosSWTBot(chatTab.widget).chatInput();
    chatInput.setText(message);
    // chatInput.pressShortcut(Keystrokes.LF);
    chatInput.pressEnterKey();
  }

  @Override
  public String getChatMessage() throws RemoteException {
    RemoteWorkbenchBot.getInstance().activateWorkbench();
    chatTab.activate();
    SarosSWTBotChatInput chatInput = new SarosSWTBot(chatTab.widget).chatInput();
    return chatInput.getText();
  }

  @Override
  public String getUserNameOnChatLinePartnerChangeSeparator() throws RemoteException {
    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);
    log.debug(
        "user name of the first chat line partner change separator: "
            + bot.chatLinePartnerChangeSeparator().getPlainID());
    return bot.chatLinePartnerChangeSeparator().getPlainID();
  }

  @Override
  public String getUserNameOnChatLinePartnerChangeSeparator(int index) throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug(
        "user name of the chat line partner change separator with the index"
            + index
            + ": "
            + bot.chatLinePartnerChangeSeparator(index).getPlainID());
    return bot.chatLinePartnerChangeSeparator(index).getPlainID();
  }

  @Override
  public String getUserNameOnChatLinePartnerChangeSeparator(String plainID) throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug(
        "user name of the chat line partner change separator with the plainID "
            + plainID
            + ": "
            + bot.chatLinePartnerChangeSeparator(plainID).getPlainID());
    return bot.chatLinePartnerChangeSeparator(plainID).getPlainID();
  }

  @Override
  public String getTextOfFirstChatLine() throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug("text of the first chat line: " + bot.chatLine().getText());
    return bot.chatLine().getText();
  }

  @Override
  public String getTextOfChatLine(int index) throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug(
        "text of the chat line with the index " + index + ": " + bot.chatLine(index).getText());
    return bot.chatLine(index).getText();
  }

  @Override
  public String getTextOfLastChatLine() throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug("text of the last chat line: " + bot.lastChatLine().getText());
    return bot.lastChatLine().getText();
  }

  @Override
  public String getTextOfChatLine(String regex) throws RemoteException {

    chatTab.activate();
    SarosSWTBot bot = new SarosSWTBot(chatTab.widget);

    log.debug("text of the chat line with the specifed regex: " + bot.chatLine(regex).getText());
    return bot.chatLine(regex).getText();
  }

  @Override
  public void waitUntilGetChatMessage(String jid, String message) throws RemoteException {
    chatTab.activate();
    new SarosSWTBot(chatTab.widget)
        .waitUntil(SarosConditions.isChatMessageExist(this, jid, message));
  }

  @Override
  public String getTitle() throws RemoteException {

    String tabTitle = chatTab.getText();
    log.debug("tab title text: " + tabTitle);
    return tabTitle;
  }

  @Override
  public boolean isActive() throws RemoteException {
    return chatTab.isActive();
  }

  @Override
  public void activate() throws RemoteException {
    chatTab.activate();
  }
}
