package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.jquery.JQueryHelper;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLMultiSelect;

public final class RemoteHTMLMultiSelect extends HTMLSTFRemoteObject
    implements IRemoteHTMLMultiSelect {

  private static final RemoteHTMLMultiSelect INSTANCE = new RemoteHTMLMultiSelect();

  public static RemoteHTMLMultiSelect getInstance() {
    return INSTANCE;
  }

  @Override
  public List<String> getSelection() throws RemoteException {
    Object selection = new JQueryHelper(browser).getFieldValue(selector);
    if (selection != null) {
      String raw = selection.toString();
      String[] items = raw.replaceAll("(\\[|\\]|\\s)", "").split(",");
      return Arrays.asList(items);
    }

    return null;
  }

  @Override
  public void select(List<String> value) throws RemoteException {
    new JQueryHelper(browser).setFieldValue(selector, value.toString());
  }

  @Override
  public int size() throws RemoteException {
    return options().size();
  }

  @Override
  public List<String> options() throws RemoteException {
    return new JQueryHelper(browser).getSelectOptions(selector);
  }
}
