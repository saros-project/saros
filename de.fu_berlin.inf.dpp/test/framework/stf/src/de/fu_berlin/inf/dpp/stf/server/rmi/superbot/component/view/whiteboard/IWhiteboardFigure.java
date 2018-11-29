package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A figure wrapper holding the most important information of an IFigure which is not serializable.
 */
public interface IWhiteboardFigure extends Remote {

  public Point getLocation() throws RemoteException;

  public Dimension getSize() throws RemoteException;

  public Color getBackgroundColor() throws RemoteException;

  public Color getForegroundColor() throws RemoteException;

  public String getType() throws RemoteException;
}
