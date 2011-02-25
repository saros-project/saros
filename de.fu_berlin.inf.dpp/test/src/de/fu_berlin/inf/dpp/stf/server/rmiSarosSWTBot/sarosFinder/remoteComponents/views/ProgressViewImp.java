package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

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
        preCondition();
        bot().view(VIEW_PROGRESS).bot().toolbarButton().click();

    }

    public void removeProcess(int index) throws RemoteException {
        preCondition();
        bot().view(VIEW_PROGRESS)
            .toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        bot().view(VIEW_PROGRESS).bot().toolbarButton(index).click();

    }

    public boolean existPorgress() throws RemoteException {
        preCondition();
        bot().view(VIEW_PROGRESS)
            .toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        return bot().view(VIEW_PROGRESS).bot().existsToolbarButton();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilProgressNotExists() throws RemoteException {
        preCondition();
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existPorgress();
            }

            public String getFailureMessage() {
                return "There are still some progresses";
            }
        });

    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    private void preCondition() throws RemoteException {
        bot().openViewById(VIEW_PROGRESS_ID);
        bot().view(VIEW_PROGRESS).show();
    }

}
