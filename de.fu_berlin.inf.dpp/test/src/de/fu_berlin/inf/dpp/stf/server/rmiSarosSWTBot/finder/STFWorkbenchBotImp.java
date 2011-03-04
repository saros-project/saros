package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditorImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotPerspectiveImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class STFWorkbenchBotImp extends STFBotImp implements STFWorkbenchBot {

    private static transient STFWorkbenchBotImp self;

    private static STFBotViewImp view;
    private static STFBotPerspectiveImp stfBotPers;
    private static STFBotEditorImp stfBotEditor;

    private static SarosSWTBot sarosSwtBot;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static STFWorkbenchBotImp getInstance() {
        if (self != null)
            return self;
        self = new STFWorkbenchBotImp();
        view = STFBotViewImp.getInstance();
        stfBotPers = STFBotPerspectiveImp.getInstance();
        stfBotEditor = STFBotEditorImp.getInstance();
        sarosSwtBot = SarosSWTBot.getInstance();
        return self;
    }

    /**********************************************
     * 
     * view
     * 
     **********************************************/
    public STFBotView view(String viewTitle) throws RemoteException {
        view.setWidget(sarosSwtBot.viewByTitle(viewTitle));
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
        for (SWTBotView view : sarosSwtBot.views())
            list.add(view.getTitle());
        return list;
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public STFBotView viewById(String id) throws RemoteException {
        view.setWidget(sarosSwtBot.viewById(id));
        return view;
    }

    public STFBotView activeView() throws RemoteException {
        return view(sarosSwtBot.activeView().getTitle());
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
        return sarosSwtBot.perspectiveById(id).isActive();
    }

    public List<String> getPerspectiveTitles() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotPerspective perspective : sarosSwtBot.perspectives())
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

    public STFBotPerspective perspectiveByLabel(String label)
        throws RemoteException {
        stfBotPers.setWidget(sarosSwtBot.perspectiveByLabel(label));
        return stfBotPers;
    }

    public STFBotPerspective perspectiveById(String id) throws RemoteException {
        stfBotPers.setWidget(sarosSwtBot.perspectiveById(id));
        return stfBotPers;
    }

    public STFBotPerspective activePerspective() throws RemoteException {
        return perspectiveByLabel(sarosSwtBot.activePerspective().getLabel());
    }

    public STFBotPerspective defaultPerspective() throws RemoteException {
        return perspectiveByLabel(sarosSwtBot.defaultPerspective().getLabel());
    }

    public void resetActivePerspective() throws RemoteException {
        sarosSwtBot.resetActivePerspective();
    }

    /**********************************************
     * 
     * editor
     * 
     **********************************************/

    public STFBotEditor editor(String fileName) throws RemoteException {
        stfBotEditor.setWidget(sarosSwtBot.editorByTitle(fileName)
            .toTextEditor());
        return stfBotEditor;
    }

    public STFBotEditor editorById(String id) throws RemoteException {
        stfBotEditor.setWidget(sarosSwtBot.editorById(id).toTextEditor());
        return stfBotEditor;
    }

    public boolean isEditorOpen(String fileName) throws RemoteException {
        for (SWTBotEditor editor : sarosSwtBot.editors()) {
            if (editor.getTitle().equals(fileName))
                return true;
        }
        return false;
    }

    public STFBotEditor activeEditor() throws RemoteException {
        return editor(sarosSwtBot.activeEditor().getTitle());

    }

    public void closeAllEditors() throws RemoteException {
        sarosSwtBot.closeAllEditors();
    }

    public void saveAllEditors() throws RemoteException {
        sarosSwtBot.saveAllEditors();
    }

    public void waitUntilEditorOpen(final String title) throws RemoteException {

        sarosSwtBot.waitUntil(new DefaultCondition() {
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
        sarosSwtBot.waitUntil(new DefaultCondition() {
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
        // saveAllEditors();
        closeAllEditors();
        openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    public void activateWorkbench() throws RemoteException {
        getWorkbench().activate().setFocus();
    }

    public SWTBotShell getWorkbench() throws RemoteException {
        SWTBotShell[] shells = sarosSwtBot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(".+? - .+")) {
                log.debug("shell found matching \"" + ".+? - .+" + "\"");
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
        sarosSwtBot.closeAllShells();
    }

}
