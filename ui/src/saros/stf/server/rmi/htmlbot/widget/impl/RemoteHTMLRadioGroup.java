package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotUtils;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLRadioGroup;

public final class RemoteHTMLRadioGroup extends HTMLSTFRemoteObject
    implements IRemoteHTMLRadioGroup {

  private static final RemoteHTMLRadioGroup INSTANCE = new RemoteHTMLRadioGroup();

  public static RemoteHTMLRadioGroup getInstance() {
    return INSTANCE;
  }

  @Override
  public String getSelected() throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    Object value = browser.syncRun(String.format("return view.getFieldValue('%s')", name));
    return value != null ? value.toString() : null;
  }

  @Override
  public void select(String value) throws RemoteException {
    String name = BotUtils.getSelectorName(selector);
    browser.syncRun(String.format("view.setFieldValue('%s', '%s')", name, value));
  }

  @Override
  public int size() throws RemoteException {
    return values().size();
  }

  @Override
  public List<String> values() throws RemoteException {
    return BotUtils.getListItemsValue(browser, selector);
  }
}
