package saros.stf.server.rmi.superbot.component.view.whiteboard;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Interface for the Saros Whiteboard View */
public interface ISarosWhiteboardView extends Remote {

  /** enumeration containing the several tools of the whiteboard */
  public enum Tool {
    RECTANGLE("Rectangle"),
    ELLIPSE("Ellipse"),
    DIAMOND("Diamond"),
    TEXT("Text"),
    ARROW("Arrow"),
    PENCIL("Pencil");

    private String label;

    private Tool(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  public void createRectangle(int x, int y, int width, int height) throws RemoteException;

  public void createObjectFromActiveTool(int x, int y, int width, int height)
      throws RemoteException;

  public void activateTool(String name) throws RemoteException;

  public IWhiteboardFigure getFigureFromEditPartID(String ID) throws RemoteException;

  public void click(int x, int y) throws RemoteException;

  public IWhiteboardFigure getFigureAt(int x, int y) throws RemoteException;

  public void clear() throws RemoteException;

  public void toggleMaximize() throws RemoteException;

  public void minimize() throws RemoteException;

  public void setZoom(int percent) throws RemoteException;
}
