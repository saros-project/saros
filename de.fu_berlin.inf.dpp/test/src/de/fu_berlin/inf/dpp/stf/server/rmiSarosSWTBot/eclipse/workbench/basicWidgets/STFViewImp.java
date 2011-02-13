package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class STFViewImp extends EclipseComponentImp implements STFView {

    private static transient STFViewImp self;
    private String viewTitle;
    private String viewId;

    private SWTBotView swtbotView;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFViewImp getInstance() {
        if (self != null)
            return self;
        self = new STFViewImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public STFBot bot_() {
        STFBotImp botImp = STFBotImp.getInstance();
        botImp.setBot(bot.viewByTitle(viewTitle).bot());
        return botImp;
    }

    public void setViewTitle(String title) throws RemoteException {
        if (this.viewTitle == null || !viewTitle.equals(title)) {
            this.viewTitle = title;
            if (viewTitlesAndIDs.containsKey(viewTitle))
                this.viewId = viewTitlesAndIDs.get(viewTitle);
            swtbotView = bot.viewByTitle(title);
        }

    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void close() throws RemoteException {
        bot.viewByTitle(viewTitle).close();
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

    public boolean isActive() throws RemoteException {
        try {
            return bot.activeView().getTitle().equals(viewTitle);
        } catch (WidgetNotFoundException e) {
            return false;
        }
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

}
