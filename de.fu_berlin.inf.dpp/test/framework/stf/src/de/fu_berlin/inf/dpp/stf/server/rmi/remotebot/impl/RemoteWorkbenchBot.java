package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotView;

public final class RemoteWorkbenchBot extends RemoteBot implements
    IRemoteWorkbenchBot {

    private static final Logger log = Logger
        .getLogger(RemoteWorkbenchBot.class);
    private static final RemoteWorkbenchBot INSTANCE = new RemoteWorkbenchBot();

    private RemoteBotView view;
    private RemoteBotPerspective perspective;
    private RemoteBotEditor editor;

    private SarosSWTBot swtWorkBenchBot;

    private RemoteBotChatLine chatLine;

    public RemoteWorkbenchBot() {
        super();
        view = RemoteBotView.getInstance();
        perspective = RemoteBotPerspective.getInstance();
        editor = RemoteBotEditor.getInstance();
        chatLine = RemoteBotChatLine.getInstance();
        swtWorkBenchBot = SarosSWTBot.getInstance();

    }

    public static RemoteWorkbenchBot getInstance() {
        return INSTANCE;
    }

    public void setWorkbenchBot(SarosSWTBot bot) {
        swtWorkBenchBot = bot;
    }

    /**********************************************
     * 
     * view
     * 
     **********************************************/
    public IRemoteBotView view(String viewTitle) throws RemoteException {
        view.setWidget(swtWorkBenchBot.viewByTitle(viewTitle));
        return view;
    }

    public void openViewById(final String viewId) throws RemoteException {
        try {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IWorkbench wb = PlatformUI.getWorkbench();
                    final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

                    IWorkbenchPage page = win.getActivePage();
                    try {
                        IViewReference[] registeredViews = page
                            .getViewReferences();
                        for (IViewReference registeredView : registeredViews) {
                            log.debug("registered view ID: "
                                + registeredView.getId());
                        }

                        page.showView(viewId);
                    } catch (PartInitException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            log.debug("Couldn't initialize " + viewId, e.getCause());
        }
    }

    public List<String> getTitlesOfOpenedViews() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : swtWorkBenchBot.views())
            list.add(view.getTitle());
        return list;
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public IRemoteBotView viewById(String id) throws RemoteException {
        view.setWidget(swtWorkBenchBot.viewById(id));
        return view;
    }

    public IRemoteBotView activeView() throws RemoteException {
        return view(swtWorkBenchBot.activeView().getTitle());
    }

    /**********************************************
     * 
     * perspective
     * 
     **********************************************/

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        return getPerspectiveTitles().contains(title);
    }

    public boolean isPerspectiveActive(String id) throws RemoteException {
        return swtWorkBenchBot.perspectiveById(id).isActive();
    }

    public List<String> getPerspectiveTitles() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotPerspective perspective : swtWorkBenchBot.perspectives())
            list.add(perspective.getLabel());
        return list;
    }

    public void openPerspectiveWithId(final String persID)
        throws RemoteException {
        if (!isPerspectiveActive(persID)) {
            try {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        final IWorkbench wb = PlatformUI.getWorkbench();
                        IPerspectiveDescriptor[] descriptors = wb
                            .getPerspectiveRegistry().getPerspectives();
                        for (IPerspectiveDescriptor per : descriptors) {
                            log.debug("installed perspective id:" + per.getId());
                        }
                        final IWorkbenchWindow win = wb
                            .getActiveWorkbenchWindow();
                        try {
                            wb.showPerspective(persID, win);
                        } catch (WorkbenchException e) {
                            log.debug("couldn't open perspective wit ID"
                                + persID, e);
                        }
                    }
                });
            } catch (IllegalArgumentException e) {
                log.debug("Couldn't initialize perspective with ID" + persID,
                    e.getCause());
            }
        }
    }

    public IRemoteBotPerspective perspectiveByLabel(String label)
        throws RemoteException {
        perspective.setWidget(swtWorkBenchBot.perspectiveByLabel(label));
        return perspective;
    }

    public IRemoteBotPerspective perspectiveById(String id)
        throws RemoteException {
        perspective.setWidget(swtWorkBenchBot.perspectiveById(id));
        return perspective;
    }

    public IRemoteBotPerspective activePerspective() throws RemoteException {
        return perspectiveByLabel(swtWorkBenchBot.activePerspective()
            .getLabel());
    }

    public IRemoteBotPerspective defaultPerspective() throws RemoteException {
        return perspectiveByLabel(swtWorkBenchBot.defaultPerspective()
            .getLabel());
    }

    public void resetActivePerspective() throws RemoteException {
        swtWorkBenchBot.resetActivePerspective();
    }

    /**********************************************
     * 
     * editor
     * 
     **********************************************/

    public IRemoteBotEditor editor(String fileName) throws RemoteException {
        editor
            .setWidget(swtWorkBenchBot.editorByTitle(fileName).toTextEditor());
        return editor;
    }

    public IRemoteBotEditor editorById(String id) throws RemoteException {
        editor.setWidget(swtWorkBenchBot.editorById(id).toTextEditor());
        return editor;
    }

    public boolean isEditorOpen(String fileName) throws RemoteException {
        for (SWTBotEditor editor : swtWorkBenchBot.editors()) {
            if (editor.getTitle().equals(fileName))
                return true;
        }
        return false;
    }

    public IRemoteBotEditor activeEditor() throws RemoteException {
        return editor(swtWorkBenchBot.activeEditor().getTitle());

    }

    public void closeAllEditors() throws RemoteException {
        swtWorkBenchBot.closeAllEditors();
    }

    public void saveAllEditors() throws RemoteException {
        swtWorkBenchBot.saveAllEditors();
    }

    public void waitUntilEditorOpen(final String title) throws RemoteException {

        swtWorkBenchBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isEditorOpen(title);
            }

            public String getFailureMessage() {
                return "The editor " + title + "is not open.";
            }
        });
    }

    public void waitUntilEditorClosed(final String title)
        throws RemoteException {
        swtWorkBenchBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isEditorOpen(title);
            }

            public String getFailureMessage() {
                return "The editor is not open.";
            }
        });
    }

    /**********************************************
     * 
     * workbench
     * 
     **********************************************/

    public void resetWorkbench() throws RemoteException {
        closeAllShells();
        closeAllEditors();
        openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

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
        final String message = "No shell found matching \"" + ".+? - .+"
            + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

    /**********************************************
     * 
     * shell
     * 
     **********************************************/

    public void closeAllShells() throws RemoteException {
        swtWorkBenchBot.closeAllShells();
    }

    /**********************************************
     * 
     * Chat
     * 
     **********************************************/
    public RemoteBotChatLine chatLine() throws RemoteException {
        return chatLine(0);
    }

    public RemoteBotChatLine chatLine(int index) throws RemoteException {
        chatLine.setWidget(swtWorkBenchBot.chatLine(index));
        return chatLine;
    }

    public RemoteBotChatLine lastChatLine() throws RemoteException {
        chatLine.setWidget(swtWorkBenchBot.lastChatLine());
        return chatLine;
    }

    public RemoteBotChatLine chatLine(final String regex)
        throws RemoteException {
        chatLine.setWidget(swtWorkBenchBot.chatLine(regex));
        return chatLine;
    }
}
