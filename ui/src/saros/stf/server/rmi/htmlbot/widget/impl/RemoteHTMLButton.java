package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLButton;

public final class RemoteHTMLButton extends HTMLSTFRemoteObject implements IRemoteHTMLButton {

  private static final RemoteHTMLButton INSTANCE = new RemoteHTMLButton();

  public static RemoteHTMLButton getInstance() {
    return INSTANCE;
  }

  @Override
  public void click() throws RemoteException {
    browser.run(String.format("%s[0].click();", selector.getStatement()));
  }

  @Override
  public String text() throws RemoteException {
    return (String) browser.syncRun("return " + selector.getStatement() + ".text();");
  }
}
