package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLRadioGroup;

public final class RemoteHTMLRadioGroup extends HTMLSTFRemoteObject
    implements IRemoteHTMLRadioGroup {

  private static final RemoteHTMLRadioGroup INSTANCE = new RemoteHTMLRadioGroup();

  public static RemoteHTMLRadioGroup getInstance() {
    return INSTANCE;
  }

  @Override
  public String getSelected() throws RemoteException {
    Object value = jQueryHelper.getFieldValue(selector);
    return value != null ? value.toString() : null;
  }

  @Override
  public void select(String value) throws RemoteException {
    jQueryHelper.setFieldValue(selector, value);
  }

  @Override
  public int size() throws RemoteException {
    return values().size();
  }

  @Override
  public List<String> values() throws RemoteException {
    return jQueryHelper.getListItemsValue(selector);
  }
}
