package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.ISarosWhiteboardView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.IWhiteboardFigure;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefView;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefViewer;
import org.eclipse.swtbot.swt.finder.SWTBot;

public class SarosWhiteboardView extends StfRemoteObject implements ISarosWhiteboardView {
  private static final SarosWhiteboardView INSTANCE = new SarosWhiteboardView();

  private SWTBotGefViewer viewer;
  private SWTBotGefView view;
  private ZoomManager zoomManager;

  public static SarosWhiteboardView getInstance() {
    return INSTANCE;
  }

  public ISarosWhiteboardView setView(SWTBotView view, SWTWorkbenchBot bot) {
    this.view = new SWTBotGefView(view.getReference(), bot);
    this.viewer = this.view.getSWTBotGefViewer();
    this.zoomManager =
        (ZoomManager) this.view.getReference().getView(true).getAdapter(ZoomManager.class);
    return this;
  }

  @Override
  public void createRectangle(int x, int y, int width, int height) throws RemoteException {
    viewer.activateTool("Rectangle");
    createObjectFromActiveTool(x, y, x + width, y + height);
  }

  @Override
  public void createObjectFromActiveTool(int x, int y, int width, int height) {
    viewer.drag(x, y, x + width - 1, y + height - 1);
  }

  @Override
  public void activateTool(String name) throws RemoteException {
    viewer.activateTool(name);
  }

  @Override
  public IWhiteboardFigure getFigureFromEditPartID(String label) throws RemoteException {
    return getWhiteboardFigure(((GraphicalEditPart) viewer.getEditPart(label)).getFigure());
  }

  @Override
  public void click(int x, int y) throws RemoteException {
    viewer.click(x, y);
  }

  /** returns the figure that is selected when clicking at the given coordinates. */
  @Override
  public IWhiteboardFigure getFigureAt(int x, int y) throws RemoteException {
    ToolEntry entry = viewer.getActiveTool();
    viewer.activateTool("Select");
    // wait for tool selection
    new SWTBot().sleep(500);
    viewer.click(x, y);
    // wait for object selection
    new SWTBot().sleep(500);
    List<SWTBotGefEditPart> editParts = viewer.selectedEditParts();
    if (editParts == null || editParts.isEmpty()) return null;
    IFigure figure = ((GraphicalEditPart) editParts.get(0).part()).getFigure();
    // restore previous whiteboard state
    viewer.select(viewer.mainEditPart());
    viewer.activateTool(entry.getLabel());
    // return figure
    return getWhiteboardFigure(figure);
  }

  /** Helper to obtain an {@link IWhiteboardFigure} from an {@link IFigure}. */
  private IWhiteboardFigure getWhiteboardFigure(final IFigure figure) {
    // create the WhiteboardFigure
    WhiteboardFigure wbFigure = WhiteboardFigure.getInstance();
    wbFigure.init(figure);
    return wbFigure;
  }

  /** clear the whiteboard content */
  @Override
  public void clear() throws RemoteException {
    for (SWTBotGefEditPart part : viewer.rootEditPart().children()) {
      part.part().performRequest(new GroupRequest(RequestConstants.REQ_DELETE));
    }
  }

  @Override
  public void toggleMaximize() throws RemoteException {
    view.bot().menu("Window").menu("Navigation").menu("Maximize Active View or Editor").click();
  }

  @Override
  public void minimize() throws RemoteException {
    view.bot().menu("Window").menu("Navigation").menu("Minimize Active View or Editor").click();
  }

  @Override
  public void setZoom(int percent) throws RemoteException {
    zoomManager.setZoom(percent / 100d);
  }
}
