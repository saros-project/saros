package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLCheckbox;

public final class RemoteHTMLCheckbox extends HTMLSTFRemoteObject implements IRemoteHTMLCheckbox {

  private static final RemoteHTMLCheckbox INSTANCE = new RemoteHTMLCheckbox();

  public static RemoteHTMLCheckbox getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isChecked() throws RemoteException {
    Boolean checked = (Boolean) jQueryHelper.getFieldValue(selector);
    return checked != null && checked;
  }

  @Override
  public void check() throws RemoteException {
    jQueryHelper.setFieldValue(selector, true);
  }

  @Override
  public void uncheck() throws RemoteException {
    jQueryHelper.setFieldValue(selector, false);
  }
}
