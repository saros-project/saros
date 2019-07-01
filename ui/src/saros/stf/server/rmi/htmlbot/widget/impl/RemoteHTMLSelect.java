package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLSelect;

public final class RemoteHTMLSelect extends HTMLSTFRemoteObject implements IRemoteHTMLSelect {

  private static final RemoteHTMLSelect INSTANCE = new RemoteHTMLSelect();

  public static RemoteHTMLSelect getInstance() {
    return INSTANCE;
  }

  @Override
  public String getSelection() throws RemoteException {
    Object selection = jQueryHelper.getFieldValue(selector);
    return selection != null ? selection.toString() : null;
  }

  @Override
  public void select(String value) throws RemoteException {
    jQueryHelper.setFieldValue(selector, value);
  }

  @Override
  public int size() throws RemoteException {
    return options().size();
  }

  @Override
  public List<String> options() throws RemoteException {
    return jQueryHelper.getSelectOptions(selector);
  }
}
