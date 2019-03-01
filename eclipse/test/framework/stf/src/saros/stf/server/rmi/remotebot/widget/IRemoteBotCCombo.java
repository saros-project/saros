package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;

public interface IRemoteBotCCombo extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */

  /**
   * Gets the context menu matching the text. @Parameters: text the text on the context
   * menu. @Returns: the menu that has the given text.
   *
   * @see SWTBotCCombo#contextMenu(String) @Throws: WidgetNotFoundException - if the widget is not
   *     found.
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
   * Sets the selection to the specified index. @Parameters: index the zero based index.
   *
   * @see SWTBotCCombo#setSelection(int)
   */
  public void setSelection(int index) throws RemoteException;

  /**
   * Gets the current selection in the combo. @Returns: the current selection in the combo box or
   * null if no item is selected.
   *
   * @see SWTBotCCombo#selection()
   * @throws RemoteException
   */
  public String selection() throws RemoteException;

  /**
   * Gets the current selection index. @Returns: the zero based index of the current selection.
   *
   * @see SWTBotCCombo#selectionIndex()
   * @throws RemoteException
   */
  public int selectionIndex() throws RemoteException;

  /**
   * Gets the current selection index.
   *
   * <p>Set the selection to the specified text.
   *
   * @param text the text to set into the combo.
   * @see SWTBotCCombo#setSelection(String)
   * @throws RemoteException
   */
  public void setSelection(String text) throws RemoteException;

  /** @see SWTBotCCombo#setText(String) */
  public void setText(String text) throws RemoteException;

  /** @see SWTBotCCombo#setFocus() */
  public void setFocus() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */

  /** @see SWTBotCCombo#isEnabled() */
  public boolean isEnabled() throws RemoteException;

  /** @see SWTBotCCombo#isVisible() */
  public boolean isVisible() throws RemoteException;

  /** @see SWTBotCCombo#isActive() */
  public boolean isActive() throws RemoteException;

  /** @see SWTBotCCombo#getText() */
  public String getText() throws RemoteException;

  /** @see SWTBotCCombo#getToolTipText() */
  public String getToolTipText() throws RemoteException;

  /** @see SWTBotCCombo#itemCount() */
  public int itemCount() throws RemoteException;

  /** @see SWTBotCCombo#items() */
  public String[] items() throws RemoteException;

  /** @see SWTBotCCombo#textLimit() */
  public int textLimit() throws RemoteException;
}
