package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class ViewImp extends EclipseComponentImp implements View {

    private static transient ViewImp viewImp;
    private String viewTitle;
    private String viewId;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static ViewImp getInstance() {
        if (viewImp != null)
            return viewImp;
        viewImp = new ViewImp();
        return viewImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void openById() throws RemoteException {
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

    public void close() throws RemoteException {
        if (isOpen()) {
            bot.viewByTitle(viewTitle).close();
        }
    }

    public void closeById(final String viewId) throws RemoteException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                final IViewPart view = page.findView(viewId);
                if (view != null) {
                    page.hideView(view);
                }
            }
        });
    }

    public void setFocus() throws RemoteException {
        try {
            bot.viewByTitle(viewTitle).setFocus();
            waitUntilIsActive();
        } catch (WidgetNotFoundException e) {
            log.warn("view not found '" + viewTitle + "'", e);
        }
    }

    public void setViewTitle(String title) throws RemoteException {
        this.viewTitle = title;
        this.viewId = viewTitlesAndIDs.get(viewTitle);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isOpen() throws RemoteException {
        return getTitlesOfOpenedViews().contains(viewTitle);
    }

    public boolean isActive() throws RemoteException {
        if (!isOpen())
            return false;
        try {
            return bot.activeView().getTitle().equals(viewTitle);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public List<String> getTitlesOfOpenedViews() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    public Bot bot() throws RemoteException {
        BotImp botImp = BotImp.getInstance();
        botImp.setBot(bot2());
        return botImp;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsActive() throws RemoteException {
        waitUntil(SarosConditions.isViewActive(bot, viewTitle));
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    public SWTBot bot2() {
        return bot.viewByTitle(viewTitle).bot();
    }

}
