package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotUtils;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLInputField;

public final class RemoteHTMLInputField extends HTMLSTFRemoteObject
    implements IRemoteHTMLInputField {

  private static final RemoteHTMLInputField INSTANCE = new RemoteHTMLInputField();

  public static RemoteHTMLInputField getInstance() {
    return INSTANCE;
  }

  @Override
  public void enter(String text) throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.val(selector, text);
    browser.run(String.format("view.setFieldValue('%s','%s')", name, text));
  }

  @Override
  public String getValue() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    Object value = browser.syncRun(String.format("return view.getFieldValue('%s')", name));
    return value != null ? value.toString() : null;
  }

  @Override
  public void clear() throws RemoteException {
    enter("");
  }
}
