package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl;

import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotView;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public final class RemoteWorkbenchBot extends RemoteBot implements IRemoteWorkbenchBot {

  private static final Logger log = Logger.getLogger(RemoteWorkbenchBot.class);
  private static final RemoteWorkbenchBot INSTANCE = new RemoteWorkbenchBot();

  private RemoteBotView view;
  private RemoteBotPerspective perspective;
  private RemoteBotEditor editor;

  private SWTWorkbenchBot swtWorkBenchBot;

  private RemoteBotChatLine chatLine;

  public RemoteWorkbenchBot() {
    super();
    view = RemoteBotView.getInstance();
    perspective = RemoteBotPerspective.getInstance();
    editor = RemoteBotEditor.getInstance();
    chatLine = RemoteBotChatLine.getInstance();
    swtWorkBenchBot = new SWTWorkbenchBot();
  }

  public static RemoteWorkbenchBot getInstance() {
    return INSTANCE;
  }

  @Override
  public IRemoteBotView view(String viewTitle) throws RemoteException {
    view.setWidget(swtWorkBenchBot.viewByPartName(viewTitle));
    return view;
  }

  @Override
  public void openViewById(final String viewId) throws RemoteException {

    log.trace("opening view with id: " + viewId);
    Display.getDefault()
        .syncExec(
            new Runnable() {
              @Override
              public void run() {

                IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                if (win == null) {
                  log.warn(
                      "no active workbench window available, cannot open view with id: " + viewId);
                  return;
                }

                try {
                  IWorkbenchPage page = win.getActivePage();
                  page.showView(viewId);
                } catch (PartInitException e) {
                  log.error("failed to open view with id: " + viewId, e);
                }
              }
            });
  }

  @Override
  public List<String> getTitlesOfOpenedViews() throws RemoteException {
    ArrayList<String> list = new ArrayList<String>();
    for (SWTBotView view : swtWorkBenchBot.views()) list.add(view.getTitle());
    return list;
  }

  @Override
  public boolean isViewOpen(String title) throws RemoteException {
    return getTitlesOfOpenedViews().contains(title);
  }

  @Override
  public IRemoteBotView viewById(String id) throws RemoteException {
    view.setWidget(swtWorkBenchBot.viewById(id));
    return view;
  }

  @Override
  public IRemoteBotView activeView() throws RemoteException {
    return view(swtWorkBenchBot.activeView().getTitle());
  }

  @Override
  public boolean isPerspectiveOpen(String title) throws RemoteException {
    return getPerspectiveTitles().contains(title);
  }

  @Override
  public boolean isPerspectiveActive(String id) throws RemoteException {
    return swtWorkBenchBot.perspectiveById(id).isActive();
  }

  @Override
  public List<String> getPerspectiveTitles() throws RemoteException {
    ArrayList<String> list = new ArrayList<String>();
    for (SWTBotPerspective perspective : swtWorkBenchBot.perspectives())
      list.add(perspective.getLabel());
    return list;
  }

  @Override
  public void openPerspectiveWithId(final String persID) throws RemoteException {
    if (!isPerspectiveActive(persID)) {
      try {
        Display.getDefault()
            .syncExec(
                new Runnable() {
                  @Override
                  public void run() {
                    final IWorkbench wb = PlatformUI.getWorkbench();
                    IPerspectiveDescriptor[] descriptors =
                        wb.getPerspectiveRegistry().getPerspectives();
                    for (IPerspectiveDescriptor per : descriptors) {
                      log.debug("installed perspective id:" + per.getId());
                    }
                    final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                    try {
                      wb.showPerspective(persID, win);
                    } catch (WorkbenchException e) {
                      log.debug("couldn't open perspective wit ID" + persID, e);
                    }
                  }
                });
      } catch (IllegalArgumentException e) {
        log.debug("Couldn't initialize perspective with ID" + persID, e.getCause());
      }
    }
  }

  @Override
  public IRemoteBotPerspective perspectiveByLabel(String label) throws RemoteException {
    perspective.setWidget(swtWorkBenchBot.perspectiveByLabel(label));
    return perspective;
  }

  @Override
  public IRemoteBotPerspective perspectiveById(String id) throws RemoteException {
    perspective.setWidget(swtWorkBenchBot.perspectiveById(id));
    return perspective;
  }

  @Override
  public IRemoteBotPerspective activePerspective() throws RemoteException {
    return perspectiveByLabel(swtWorkBenchBot.activePerspective().getLabel());
  }

  @Override
  public IRemoteBotPerspective defaultPerspective() throws RemoteException {
    return perspectiveByLabel(swtWorkBenchBot.defaultPerspective().getLabel());
  }

  @Override
  public void resetActivePerspective() throws RemoteException {
    swtWorkBenchBot.resetActivePerspective();
  }

  @Override
  public IRemoteBotEditor editor(String fileName) throws RemoteException {
    editor.setWidget(swtWorkBenchBot.editorByTitle(fileName).toTextEditor());
    return editor;
  }

  @Override
  public IRemoteBotEditor editorById(String id) throws RemoteException {
    editor.setWidget(swtWorkBenchBot.editorById(id).toTextEditor());
    return editor;
  }

  @Override
  public boolean isEditorOpen(String fileName) throws RemoteException {
    for (SWTBotEditor editor : swtWorkBenchBot.editors()) {
      if (editor.getTitle().equals(fileName)) return true;
    }
    return false;
  }

  @Override
  public IRemoteBotEditor activeEditor() throws RemoteException {
    return editor(swtWorkBenchBot.activeEditor().getTitle());
  }

  @Override
  public void closeAllEditors() throws RemoteException {
    swtWorkBenchBot.closeAllEditors();
  }

  @Override
  public void saveAllEditors() throws RemoteException {
    swtWorkBenchBot.saveAllEditors();
  }

  @Override
  public void waitUntilEditorOpen(final String title) throws RemoteException {

    swtWorkBenchBot.waitUntil(
        new DefaultCondition() {
          @Override
          public boolean test() throws Exception {
            return isEditorOpen(title);
          }

          @Override
          public String getFailureMessage() {
            return "The editor " + title + "is not open.";
          }
        });
  }

  @Override
  public void waitUntilEditorClosed(final String title) throws RemoteException {
    swtWorkBenchBot.waitUntil(
        new DefaultCondition() {
          @Override
          public boolean test() throws Exception {
            return !isEditorOpen(title);
          }

          @Override
          public String getFailureMessage() {
            return "The editor is not open.";
          }
        });
  }

  @Override
  public void resetWorkbench() throws RemoteException {
    closeAllShells();
    closeAllEditors();
    openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
  }

  @Override
  public void activateWorkbench() throws RemoteException {
    getWorkbench().activate().setFocus();
  }

  public SWTBotShell getWorkbench() throws RemoteException {
    SWTBotShell[] shells = swtWorkBenchBot.shells();
    for (SWTBotShell shell : shells) {
      if (shell.getText().matches(".+? - .+")) {
        log.debug("found workbench " + shell.getText());
        return shell;
      }
    }
    final String message = "No shell found matching \"" + ".+? - .+" + "\"!";
    log.error(message);
    throw new RemoteException(message);
  }

  @Override
  public void closeAllShells() throws RemoteException {

    for (int i = 0; i < 5; i++) {
      log.trace("try to close all shells with default SWTWorkbenchBot");
      try {
        swtWorkBenchBot.closeAllShells();
        return;
      } catch (TimeoutException closeAllShellsTimeout) {
        log.warn(
            "default SWTWorkbenchBot could not close all shells, trying to resolve the problem",
            closeAllShellsTimeout);
      }

      try {
        closeShell(swtWorkBenchBot.activeShell());
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      try {
        for (SWTBotShell shell : swtWorkBenchBot.shells()) closeShell(shell);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      // wait for shell(s) to update
      swtWorkBenchBot.sleep(SWTBotPreferences.TIMEOUT);
    }

    swtWorkBenchBot.closeAllShells();
  }

  @Override
  public RemoteBotChatLine chatLine() throws RemoteException {
    return chatLine(0);
  }

  @Override
  public RemoteBotChatLine chatLine(int index) throws RemoteException {
    chatLine.setWidget(new SarosSWTBot().chatLine(index));
    return chatLine;
  }

  @Override
  public RemoteBotChatLine lastChatLine() throws RemoteException {
    chatLine.setWidget(new SarosSWTBot().lastChatLine());
    return chatLine;
  }

  @Override
  public RemoteBotChatLine chatLine(final String regex) throws RemoteException {
    chatLine.setWidget(new SarosSWTBot().chatLine(regex));
    return chatLine;
  }

  @Override
  public void resetBot() throws RemoteException {
    this.setBot(new SWTBot());
  }

  private void closeShell(SWTBotShell shell) {
    // TODO shell names currently hard coded

    try {

      String shellName = shell.getText();

      try {
        shell.activate();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }

      if (shellName.equals(SHELL_CONFIRM_DECLINE_INVITATION)) {
        shell.bot().button(YES).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
      }

      if (shellName.equals("File Changed")) {
        shell.bot().button(NO).click();
      }

      if (shellName.equals("Wizard Closing")) {
        shell.bot().button(OK).click();
      }

      if (shellName.equals(SHELL_NEED_BASED_SYNC)) {
        shell.bot().button(NO).click();
      }

      if (shellName.contains(SHELL_MONITOR_PROJECT_SYNCHRONIZATION)) {
        shell.bot().button(CANCEL).click();
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
