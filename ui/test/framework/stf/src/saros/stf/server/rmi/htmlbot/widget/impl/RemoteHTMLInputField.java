package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.jquery.JQueryHelper;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLInputField;

public final class RemoteHTMLInputField extends HTMLSTFRemoteObject
    implements IRemoteHTMLInputField {

  private static final RemoteHTMLInputField INSTANCE = new RemoteHTMLInputField();

  public static RemoteHTMLInputField getInstance() {
    return INSTANCE;
  }

  @Override
  public void enter(String text) throws RemoteException {
    browser.val(selector, text);
    new JQueryHelper(browser).setFieldValue(selector, text);
  }

  @Override
  public String getValue() throws RemoteException {
    Object value = new JQueryHelper(browser).getFieldValue(selector);
    return value != null ? value.toString() : null;
  }

  @Override
  public void clear() throws RemoteException {
    enter("");
  }
}
