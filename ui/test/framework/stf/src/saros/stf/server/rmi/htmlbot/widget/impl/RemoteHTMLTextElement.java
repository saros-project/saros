package saros.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLTextElement;

public final class RemoteHTMLTextElement extends HTMLSTFRemoteObject
    implements IRemoteHTMLTextElement {

  private static final RemoteHTMLTextElement INSTANCE = new RemoteHTMLTextElement();

  public static RemoteHTMLTextElement getInstance() {
    return INSTANCE;
  }

  @Override
  public String getText() throws RemoteException {

    Object text = browser.syncRun(String.format("return %s.text()", selector.getStatement()));
    return text != null ? text.toString() : null;
  }

  @Override
  public void setText(String text) throws RemoteException {
    browser.run(String.format("%s.text('%s')", selector.getStatement(), text));
  }
}
