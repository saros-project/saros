package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.BotUtils;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLCheckbox;
import java.rmi.RemoteException;

public final class RemoteHTMLCheckbox extends HTMLSTFRemoteObject implements IRemoteHTMLCheckbox {

  private static final RemoteHTMLCheckbox INSTANCE = new RemoteHTMLCheckbox();

  public static RemoteHTMLCheckbox getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isChecked() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    Object checked = browser.syncRun(String.format("return view.getFieldValue('%s')", name));
    return checked != null ? (Boolean) checked : null;
  }

  @Override
  public void check() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.run(String.format("view.setFieldValue('%s', true)", name));
  }

  @Override
  public void uncheck() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.run(String.format("view.setFieldValue('%s', false)", name));
  }
}
