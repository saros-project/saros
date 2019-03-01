package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotUtils;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLSelect;

public final class RemoteHTMLSelect extends HTMLSTFRemoteObject implements IRemoteHTMLSelect {

  private static final RemoteHTMLSelect INSTANCE = new RemoteHTMLSelect();

  public static RemoteHTMLSelect getInstance() {
    return INSTANCE;
  }

  @Override
  public String getSelection() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    Object selection = browser.syncRun(String.format("return view.getFieldValue('%s')", name));
    return selection != null ? selection.toString() : null;
  }

  @Override
  public void select(String value) throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.syncRun(String.format("view.setFieldValue('%s', '%s')", name, value));
  }

  @Override
  public int size() throws RemoteException {
    return options().size();
  }

  @Override
  public List<String> options() throws RemoteException {
    return BotUtils.getSelectOptions(browser, selector);
  }
}
