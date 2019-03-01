package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.hamcrest.Matcher;

public interface IRemoteBotToolbarDropDownButton extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  public IRemoteBotMenu menuItem(String menuItem) throws RemoteException;

  public IRemoteBotMenu menuItem(Matcher<MenuItem> matcher) throws RemoteException;

  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void click() throws RemoteException;

  public void clickAndWait() throws RemoteException;

  public void setFocus() throws RemoteException;

  public void pressShortcut(KeyStroke... keys) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public boolean isEnabled() throws RemoteException;

  public boolean isVisible() throws RemoteException;

  public boolean isActive() throws RemoteException;

  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>waits until
   *
   * <p>********************************************
   */
  public void waitUntilIsEnabled() throws RemoteException;
}
