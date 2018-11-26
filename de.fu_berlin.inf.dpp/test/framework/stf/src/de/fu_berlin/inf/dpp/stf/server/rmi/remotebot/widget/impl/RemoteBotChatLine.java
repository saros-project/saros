package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.SarosSWTBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotChatLine;
import java.rmi.RemoteException;

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
