package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteBotStyledText extends Remote {

  /**
   * ********************************************
   *
   * <p>finders
   *
   * <p>********************************************
   */
  public IRemoteBotMenu contextMenu(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>states
   *
   * <p>********************************************
   */
  public String getText() throws RemoteException;

  public String getToolTipText() throws RemoteException;

  public String getTextOnCurrentLine() throws RemoteException;

  public String getSelection() throws RemoteException;

  public List<String> getLines() throws RemoteException;

  public int getLineCount() throws RemoteException;
}
