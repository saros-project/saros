package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class ViewImp extends EclipsePart implements View {

    private static transient ViewImp viewImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static ViewImp getInstance() {
        if (viewImp != null)
            return viewImp;
        viewImp = new ViewImp();
        return viewImp;
    }

    // actions
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

    public void closeViewByTitle(String title) throws RemoteException {
        if (isViewOpen(title)) {
            bot.viewByTitle(title).close();
        }
    }

    public void closeViewById(final String viewId) throws RemoteException {
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

    public void setFocusOnViewByTitle(String title) throws RemoteException {
        try {
            bot.viewByTitle(title).setFocus();
        } catch (WidgetNotFoundException e) {
            log.warn("view not found '" + title + "'", e);
        }
    }

    // states
    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public boolean isViewActive(String title) throws RemoteException {
        if (!isViewOpen(title))
            return false;
        try {
            return bot.activeView().getTitle().equals(title);
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

    // waits until
    public void waitUntilViewActive(String viewName) throws RemoteException {
        waitUntil(SarosConditions.isViewActive(bot, viewName));
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @return the {@link SWTBotView} specified with the given title.
     */
    public SWTBotView getView(String viewTitle) {
        return bot.viewByTitle(viewTitle);
    }
}
