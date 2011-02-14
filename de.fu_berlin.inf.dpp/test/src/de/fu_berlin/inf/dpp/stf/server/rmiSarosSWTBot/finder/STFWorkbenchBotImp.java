package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

public class STFWorkbenchBotImp extends STFBotImp implements STFWorkbenchBot {

    private static transient STFWorkbenchBotImp self;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static STFWorkbenchBotImp getInstance() {
        if (self != null)
            return self;
        self = new STFWorkbenchBotImp();

        return self;
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public STFBotView view(String viewTitle) throws RemoteException {
        new SWTWorkbenchBot();
        stfView.setViewTitle(viewTitle);
        return stfView;
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
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public STFBotPerspective perspectiveByLabel(String label)
        throws RemoteException {
        stfPerspective.setLabel(label);
        return stfPerspective;
    }

    public STFBotPerspective perspectiveById(String id) throws RemoteException {
        stfPerspective.setId(id);
        return stfPerspective;
    }

    public STFBotView viewById(String id) throws RemoteException {
        stfView.setId(id);
        return stfView;
    }

    public STFBotView activeView() throws RemoteException {
        return view(bot.activeView().getTitle());
    }

    public STFBotEditor editor(String fileName) throws RemoteException {
        stfEditor.setTitle(fileName);
        return stfEditor;
    }

    public STFBotEditor editorById(String id) throws RemoteException {
        stfEditor.setId(id);
        return stfEditor;
    }

    public boolean isEditorOpen(String fileName) throws RemoteException {
        for (SWTBotEditor editor : bot.editors()) {
            if (editor.getTitle().equals(fileName))
                return true;
        }
        return false;
    }

    public STFBotEditor activeEditor() throws RemoteException {
        return editor(bot.activeEditor().getTitle());

    }

    public STFBotPerspective activePerspective() throws RemoteException {
        return perspectiveByLabel(bot.activePerspective().getLabel());
    }

    public void resetWorkbench() throws RemoteException {
        bot.resetWorkbench();

    }

    public STFBotPerspective defaultPerspective() throws RemoteException {
        return perspectiveByLabel(bot.defaultPerspective().getLabel());
    }

    public void closeAllEditors() throws RemoteException {
        bot.closeAllEditors();
    }

    public void saveAllEditors() throws RemoteException {
        bot.saveAllEditors();
    }

    public void resetActivePerspective() throws RemoteException {
        bot.resetActivePerspective();
    }

    public void closeAllShells() throws RemoteException {
        bot.closeAllShells();

    }

}
