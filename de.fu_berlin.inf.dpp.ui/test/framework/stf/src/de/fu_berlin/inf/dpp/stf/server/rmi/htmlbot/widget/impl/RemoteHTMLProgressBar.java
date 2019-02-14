package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.BotUtils;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLProgressBar;
import java.rmi.RemoteException;

public final class RemoteHTMLProgressBar extends HTMLSTFRemoteObject
    implements IRemoteHTMLProgressBar {

  private static final RemoteHTMLProgressBar INSTANCE = new RemoteHTMLProgressBar();

  public static RemoteHTMLProgressBar getInstance() {
    return INSTANCE;
  }

  @Override
  public int getValue() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    Object selection = browser.syncRun(String.format("return view.getFieldValue('%s')", name));
    return selection != null ? ((Double) selection).intValue() : null;
  }

  @Override
  public void setValue(int value) throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.syncRun(String.format("view.setFieldValue('%s',%d)", name, value));
  }
}
