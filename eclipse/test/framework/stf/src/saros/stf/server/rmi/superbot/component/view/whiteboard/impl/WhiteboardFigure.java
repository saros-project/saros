package saros.stf.server.rmi.superbot.component.view.whiteboard.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.rmi.RemoteException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.superbot.component.view.whiteboard.IWhiteboardFigure;

/**
 * Implementation of the IWhiteboardFigure providing figure information being necessary for testing.
 */
public class WhiteboardFigure extends StfRemoteObject implements IWhiteboardFigure {

  private static final WhiteboardFigure INSTANCE = new WhiteboardFigure();

  public static WhiteboardFigure getInstance() {
    return INSTANCE;
  }

  private String type;
  private Point location;
  private Dimension size;
  private Color backgroundColor;
  private Color foregroundColor;

  /**
   * initializes this WhiteboardFigure object with the values from the passed IFigure.
   *
   * @param figure
   */
  public void init(final IFigure figure) {
    Point location =
        new Point(figure.getBounds().getLocation().x, figure.getBounds().getLocation().y);
    Dimension size =
        new Dimension(figure.getBounds().getSize().width, figure.getBounds().getSize().height);
    // initialize the colors in the swt thread to properly obtain color
    // information from figure. throws an Exception otherwise
    Color foregroundColor;
    Color backgroundColor;
    backgroundColor =
        UIThreadRunnable.syncExec(
            new Result<Color>() {
              @Override
              public Color run() {
                org.eclipse.swt.graphics.Color color = figure.getBackgroundColor();
                return new Color(color.getRed(), color.getGreen(), color.getRed());
              }
            });
    foregroundColor =
        UIThreadRunnable.syncExec(
            new Result<Color>() {
              @Override
              public Color run() {
                org.eclipse.swt.graphics.Color color = figure.getForegroundColor();
                return new Color(color.getRed(), color.getGreen(), color.getRed());
              }
            });
    // update the fields
    this.type = figure.getClass().toString();
    this.location = location;
    this.size = size;
    this.backgroundColor = backgroundColor;
    this.foregroundColor = foregroundColor;
  }

  @Override
  public Point getLocation() throws RemoteException {
    return location;
  }

  @Override
  public Dimension getSize() throws RemoteException {
    return size;
  }

  @Override
  public Color getBackgroundColor() throws RemoteException {
    return backgroundColor;
  }

  @Override
  public Color getForegroundColor() throws RemoteException {
    return foregroundColor;
  }

  @Override
  public String getType() throws RemoteException {
    return type;
  }
}
