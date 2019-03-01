package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

public interface IRemoteBotButton extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  /**
   * Gets the context menu matching the text.
   *
   * @param text the text on the context menu.
   * @return the menu that has the given text.
   * @throws WidgetNotFoundException if the widget is not found.
   * @see SWTBotButton#contextMenu(String)
   */
  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */

  /**
   * Click on the button.
   *
   * @see SWTBotButton#click()
   */
  public void click() throws RemoteException;

  /**
   * Click the button until it is enabled
   *
   * @throws RemoteException
   */
  public void clickAndWait() throws RemoteException;

  /**
   * Sets the focus on this control.
   *
   * @see SWTBotButton#setFocus()
   * @throws RemoteException
   */
  public void setFocus() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */

  /**
   * Gets if the object's widget is enabled. @Returns: true if the widget is enabled.
   *
   * @see SWTBotButton#isEnabled()
   * @throws RemoteException
   */
  public boolean isEnabled() throws RemoteException;

  /**
   * Checks if the widget is visible. @Returns: true if the widget is visible, false otherwise.
   *
   * @see SWTBotButton#isVisible()
   * @throws RemoteException
   */
  public boolean isVisible() throws RemoteException;

  /**
   * @Returns: true if this widget has focus.
   *
   * @see SWTBotButton#isActive()
   * @throws RemoteException
   */
  public boolean isActive() throws RemoteException;

  /**
   * Gets the text of this object's widget. @Returns: the text on the widget.
   *
   * @see SWTBotButton#getText()
   * @throws RemoteException
   */
  public String getText() throws RemoteException;

  /**
   * Gets the tooltip of this object's widget. @Returns: the tooltip on the widget.
   *
   * @see SWTBotButton#getToolTipText()
   * @throws RemoteException
   */
  public String getToolTipText() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>waits until
   *
   * <p>********************************************
   */
  /** Wait until the button is enabled. */
  public void waitUntilIsEnabled() throws RemoteException;

  /**
   * Wait long until the button is enabled.
   *
   * @throws RemoteException
   */
  public void waitLongUntilIsEnabled() throws RemoteException;
}
