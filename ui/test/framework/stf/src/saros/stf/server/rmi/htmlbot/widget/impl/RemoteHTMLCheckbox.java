package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotUtils;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLCheckbox;

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
