package saros.stf.server.rmi.remotebot;

import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.hamcrest.Matcher;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotPerspective;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotView;
import saros.stf.server.rmi.remotebot.widget.impl.RemoteBotChatLine;

public interface IRemoteWorkbenchBot extends IRemoteBot {
  public IRemoteBotView view(String viewTitle) throws RemoteException;

  /**
   * @return the title list of all the views which are opened currently.
   * @see SWTWorkbenchBot#views()
   */
  public List<String> getTitlesOfOpenedViews() throws RemoteException;

  public boolean isViewOpen(String title) throws RemoteException;

  /**
   * open the given view specified with the viewId.
   *
   * @param viewId the id of the view, which you want to open.
   */
  public void openViewById(String viewId) throws RemoteException;

  /**
   * Shortcut for perspective(withPerspectiveLabel(label))
   *
   * @param label the "human readable" label for the perspective
   * @return a perspective with the specified <code>label</code>
   * @see WidgetMatcherFactory#withPerspectiveLabel(Matcher)
   */
  public IRemoteBotPerspective perspectiveByLabel(String label) throws RemoteException;

  public IRemoteBotPerspective perspectiveById(String id) throws RemoteException;

  /**
   * Shortcut for view(withPartId(id))
   *
   * @param id the view id
   * @return the view with the specified id
   * @see WidgetMatcherFactory#withPartId(String)
   */
  public IRemoteBotView viewById(String id) throws RemoteException;

  /**
   * Returns the active workbench view part
   *
   * @return the active view, if any
   * @throws WidgetNotFoundException if there is no active view
   */
  public IRemoteBotView activeView() throws RemoteException;

  /**
   * Shortcut for editor(withPartName(title))
   *
   * @param fileName the the filename on the editor tab
   * @return the editor with the specified title
   */
  public IRemoteBotEditor editor(String fileName) throws RemoteException;

  /**
   * Shortcut for editor(withPartId(id))
   *
   * @param id the the id on the editor tab
   * @return the editor with the specified title
   */
  public IRemoteBotEditor editorById(String id) throws RemoteException;

  public boolean isEditorOpen(String fileName) throws RemoteException;

  /**
   * Returns the active workbench editor part
   *
   * @return the active editor, if any
   * @throws WidgetNotFoundException if there is no active view
   */
  public IRemoteBotEditor activeEditor() throws RemoteException;

  /**
   * @param title the title of a perspective.
   * @return<tt>true</tt>, if the perspective specified with the given title is open.
   */
  public boolean isPerspectiveOpen(String title) throws RemoteException;

  /**
   * @param id id which identify a perspective
   * @return<tt>true</tt>, if the perspective specified with the given id is active.
   */
  public boolean isPerspectiveActive(String id) throws RemoteException;

  /** @return titles of all available perspectives. */
  public List<String> getPerspectiveTitles() throws RemoteException;

  /**
   * Open a perspective using Window->Open Perspective->Other... The method is defined as helper
   * method for other openPerspective* methods and should not be exported using rmi.
   *
   * <p>1. if the perspective already exist, return.
   *
   * <p>2. activate the saros-instance-window(alice / bob / carl). If the workbench isn't active,
   * delegate can't find the main menus.
   *
   * <p>3. click main menus Window -> Open perspective -> Other....
   *
   * <p>4. confirm the pop-up window "Open Perspective".
   *
   * @param persID example: "org.eclipse.jdt.ui.JavaPerspective"
   */
  public void openPerspectiveWithId(final String persID) throws RemoteException;

  /** @return the active perspective in the active workbench page */
  public IRemoteBotPerspective activePerspective() throws RemoteException;

  /**
   * Does a <em>best effort</em> to reset the workbench. This method attempts to:
   *
   * <ul>
   *   <li>close all non-workbench windows
   *   <li>save and close all open editors
   *   <li>reset the <em>active</em> perspective
   *   <li>switch to the default perspective for the workbench
   *   <li>reset the <em>default</em> perspective for the workbench
   *       <ul>
   */
  public void resetWorkbench() throws RemoteException;

  /**
   * Activate the saros-instance.This method is very useful, wenn you test saros under MAC
   *
   * @throws RemoteException
   */
  public void activateWorkbench() throws RemoteException;

  /** Returns the default perspective as defined in the WorkbenchAdvisor of the application. */
  public IRemoteBotPerspective defaultPerspective() throws RemoteException;

  public void closeAllEditors() throws RemoteException;

  public void saveAllEditors() throws RemoteException;

  public void resetActivePerspective() throws RemoteException;

  public void waitUntilEditorOpen(final String title) throws RemoteException;

  public void waitUntilEditorClosed(final String title) throws RemoteException;

  public void closeAllShells() throws RemoteException;

  /*
   * chat
   */

  public RemoteBotChatLine chatLine() throws RemoteException;

  public RemoteBotChatLine chatLine(int index) throws RemoteException;

  public RemoteBotChatLine lastChatLine() throws RemoteException;

  public RemoteBotChatLine chatLine(final String regex) throws RemoteException;

  /**
   * For internal use, do not use this method
   *
   * @throws RemoteException
   */
  public void resetBot() throws RemoteException;
}
