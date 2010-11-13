package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class ProgressViewObjectImp extends EclipseObject implements
    ProgressViewObject {

    // public static ProgressViewObjectImp classVariable;

    private static transient ProgressViewObjectImp self;

    /**
     * {@link ProgressViewObjectImp} is a singleton, but inheritance is
     * possible.
     */
    public static ProgressViewObjectImp getInstance() {
        if (self != null)
            return self;
        self = new ProgressViewObjectImp();
        return self;
    }

    public void openProgressView() throws RemoteException {
        viewObject.openViewById("org.eclipse.ui.views.ProgressView");
    }

    public void activateProgressView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(PGViewName);
    }

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
        SWTBotView view = bot.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public boolean isProgressViewOpen() throws RemoteException {
        return viewObject.isViewOpen("Progress");
    }

    /**
     * end the invitation process. ie. Click the red stop icon in Progress view.
     */
    public void cancelInvitation() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = bot.viewByTitle("Progress");
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton();
        b.click();
    }

    public void cancelInvitation(int index) throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = bot.viewByTitle("Progress");
        view.toolbarButton("Remove All Finished Operations").click();
        view.setFocus();
        SWTBot bot = view.bot();
        SWTBotToolbarButton b = bot.toolbarButton(index);
        b.click();
    }

    /**
     * For some tests a host need to invite many peers concurrently and some
     * operations should not be performed if the invitation processes aren't
     * finished yet. In this case, you can use this method to guarantee, that
     * host wait so long until all the invitation Processes are finished.
     */
    public void waitUntilNoInvitationProgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        bot.waitUntil(SarosConditions.existNoInvitationProgress(bot), 100000);
    }

}
