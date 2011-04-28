package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ProgressView extends Component implements IProgressView {
    private static transient ProgressView self;

    private IRemoteBotView view;

    /**
     * {@link ProgressView} is a singleton, but inheritance is possible.
     */
    public static ProgressView getInstance() {
        if (self != null)
            return self;
        self = new ProgressView();
        return self;
    }

    public IProgressView setView(IRemoteBotView view) {
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
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existsPorgress();
            }

            public String getFailureMessage() {
                return "There are still some progresses existed";
            }
        });

    }

}
