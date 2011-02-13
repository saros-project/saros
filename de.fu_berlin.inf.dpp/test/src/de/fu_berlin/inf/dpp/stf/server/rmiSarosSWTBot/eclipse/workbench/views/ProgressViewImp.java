package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

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
        preCondition();
        stfToolbarButton.clickToolbarButtonInView(VIEW_PROGRESS);
    }

    public void removeProcess(int index) throws RemoteException {
        preCondition();
        stfToolbarButton.clickToolbarButtonWithTooltipOnView(VIEW_PROGRESS,
            TB_REMOVE_ALL_FINISHED_OPERATIONS);
        stfToolbarButton
            .clickToolbarButtonWithIndexInView(VIEW_PROGRESS, index);
    }

    public boolean existPorgress() throws RemoteException {
        preCondition();
        stfToolbarButton.clickToolbarButtonWithTooltipOnView(VIEW_PROGRESS,
            TB_REMOVE_ALL_FINISHED_OPERATIONS);
        return stfToolbarButton.existstoolbarButonInView(VIEW_PROGRESS);
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilProgressNotExists() throws RemoteException {
        preCondition();
        waitUntil(new DefaultCondition() {
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
        bot().openById(VIEW_PROGRESS_ID);
        bot().view(VIEW_PROGRESS).setFocus();
    }

}
