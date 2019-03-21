package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.jquery.JQueryHelper;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLProgressBar;

public final class RemoteHTMLProgressBar extends HTMLSTFRemoteObject
    implements IRemoteHTMLProgressBar {

  private static final RemoteHTMLProgressBar INSTANCE = new RemoteHTMLProgressBar();

  public static RemoteHTMLProgressBar getInstance() {
    return INSTANCE;
  }

  @Override
  public int getValue() throws RemoteException {
    Object selection = new JQueryHelper(browser).getFieldValue(selector);
    return selection != null ? ((Double) selection).intValue() : null;
  }

  @Override
  public void setValue(int value) throws RemoteException {
    new JQueryHelper(browser).setFieldValue(selector, value);
  }
}
