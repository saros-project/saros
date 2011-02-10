package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class ProgressViewImp extends EclipseComponentImp implements
    ProgressView {

    private static transient ProgressViewImp self;

    /**
     * {@link ProgressViewImp} is a singleton, but inheritance is possible.
     */
    public static ProgressViewImp getInstance() {
        if (self != null)
            return self;
        self = new ProgressViewImp();
        return self;
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

    public void removeProgress() throws RemoteException {
        viewW.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        viewW.setFocusOnViewByTitle(VIEW_PROGRESS);
        SWTBotView view = bot.viewByTitle(VIEW_PROGRESS);
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void removeProcess(int index) throws RemoteException {
        preCondition();
        SWTBotView view = bot.viewByTitle(VIEW_PROGRESS);
        view.toolbarButton("Remove All Finished Operations").click();
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton(index);
        b.click();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isProgressViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEW_PROGRESS);
    }

    public boolean existPorgress() throws RemoteException {
        viewW.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        viewW.setFocusOnViewByTitle(VIEW_PROGRESS);
        SWTBotView view = bot.viewByTitle("Progress");
        view.setFocus();
        view.toolbarButton("Remove All Finished Operations").click();
        SWTBot bot = view.bot();
        try {
            bot.toolbarButton();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilNoInvitationProgress() throws RemoteException {
        viewW.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        viewW.setFocusOnViewByTitle(VIEW_PROGRESS);
        bot.waitUntil(SarosConditions.existNoInvitationProgress(bot), 100000);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    private void preCondition() throws RemoteException {
        viewW.openViewById(VIEW_PACKAGE_EXPLORER_ID);
        viewW.setFocusOnViewByTitle(VIEW_PROGRESS);
    }

}
