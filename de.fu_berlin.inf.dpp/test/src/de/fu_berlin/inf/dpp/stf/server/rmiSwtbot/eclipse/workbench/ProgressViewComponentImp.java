package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class ProgressViewComponentImp extends EclipseComponent implements
    ProgressViewComponent {

    private static transient ProgressViewComponentImp self;

    protected final static String VIEWNAME = "Progress";

    /**
     * {@link ProgressViewComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static ProgressViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new ProgressViewComponentImp();
        return self;
    }

    /**********************************************
     * 
     * open/close/activate the progress view
     * 
     **********************************************/

    public void openProgressView() throws RemoteException {
        basic.openViewById("org.eclipse.ui.views.ProgressView");
    }

    public void activateProgressView() throws RemoteException {
        basic.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isProgressViewOpen() throws RemoteException {
        return basic.isViewOpen("Progress");
    }

    /**********************************************
     * 
     * remove progress
     * 
     **********************************************/
    public boolean existPorgress() throws RemoteException {
        openProgressView();
        activateProgressView();
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

    /**
     * remove the progress. ie. Click the gray clubs delete icon.
     */
    public void removeProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = bot.viewByTitle(VIEWNAME);
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void removeProcess(int index) throws RemoteException {
        preCondition();
        SWTBotView view = bot.viewByTitle(VIEWNAME);
        view.toolbarButton("Remove All Finished Operations").click();
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton(index);
        b.click();
    }

    public void waitUntilNoInvitationProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        bot.waitUntil(SarosConditions.existNoInvitationProgress(bot), 100000);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    private void preCondition() throws RemoteException {
        openProgressView();
        activateProgressView();
    }

}
