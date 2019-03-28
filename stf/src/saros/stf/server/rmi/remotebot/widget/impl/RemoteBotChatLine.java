package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.bot.widget.SarosSWTBotChatLine;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotChatLine;

public final class RemoteBotChatLine extends StfRemoteObject implements IRemoteBotChatLine {

  private static final RemoteBotChatLine INSTANCE = new RemoteBotChatLine();

  private SarosSWTBotChatLine widget;

  public static RemoteBotChatLine getInstance() {
    return INSTANCE;
  }

  public IRemoteBotChatLine setWidget(SarosSWTBotChatLine ccomb) {
    this.widget = ccomb;
    return this;
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }
}
