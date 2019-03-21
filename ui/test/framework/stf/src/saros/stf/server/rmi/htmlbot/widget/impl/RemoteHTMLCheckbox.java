package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.jquery.JQueryHelper;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLCheckbox;

public final class RemoteHTMLCheckbox extends HTMLSTFRemoteObject implements IRemoteHTMLCheckbox {

  private static final RemoteHTMLCheckbox INSTANCE = new RemoteHTMLCheckbox();

  public static RemoteHTMLCheckbox getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isChecked() throws RemoteException {
    Object checked = new JQueryHelper(browser).getFieldValue(selector);
    return checked != null ? (Boolean) checked : null;
  }

  @Override
  public void check() throws RemoteException {
    new JQueryHelper(browser).setFieldValue(selector, true);
  }

  @Override
  public void uncheck() throws RemoteException {
    new JQueryHelper(browser).setFieldValue(selector, false);
  }
}
