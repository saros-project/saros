package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

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

    public STFView view(String viewTitle) throws RemoteException {
        stfView.setViewTitle(viewTitle);
        return stfView;
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public void openById(final String viewId) throws RemoteException {
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

}
