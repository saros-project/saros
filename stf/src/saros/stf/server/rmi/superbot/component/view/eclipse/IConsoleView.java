package saros.stf.server.rmi.superbot.component.view.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains convenience API to perform actions inside the console view.
 *
 * @author lchen
 * @author srossbach
 */
public interface IConsoleView extends Remote {

  /**
   * @return<tt>true</tt>, if there are text existed in the console.
   * @throws RemoteException
   */
  public boolean existTextInConsole() throws RemoteException;

  /**
   * @return the first styledText in the view Console
   * @throws RemoteException
   */
  public String getFirstTextInConsole() throws RemoteException;

  /**
   * Wait until the condition {@link IConsoleView#existTextInConsole()} is true
   *
   * @throws RemoteException
   */
  public void waitUntilExistsTextInConsole() throws RemoteException;

  /**
   * Clears all entries of the current console.
   *
   * @throws RemoteException
   */
  public void clearCurrentConsole() throws RemoteException;

  /** Waits until the current console contains the specific text. */
  public void waitUntilCurrentConsoleContainsText(String text) throws RemoteException;
}
