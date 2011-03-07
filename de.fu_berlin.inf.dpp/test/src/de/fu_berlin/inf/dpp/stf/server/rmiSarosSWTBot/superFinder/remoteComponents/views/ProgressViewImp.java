package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ProgressViewImp extends Component implements ProgressView {
    private static transient ProgressViewImp self;

    private RemoteBotView view;

    /**
     * {@link ProgressViewImp} is a singleton, but inheritance is possible.
     */
    public static ProgressViewImp getInstance() {
        if (self != null)
            return self;
        self = new ProgressViewImp();
        return self;
    }

    public ProgressView setView(RemoteBotView view) {
        this.view = view;

        return this;
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
        view.bot().toolbarButton().click();

    }

    public void removeProcess(int index) throws RemoteException {
        view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        view.bot().toolbarButton(index).click();

    }

    public boolean existsPorgress() throws RemoteException {
        view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
        return view.bot().existsToolbarButton();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilNotExistsProgress() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existsPorgress();
            }

            public String getFailureMessage() {
                return "There are still some progresses existed";
            }
        });

    }

}
