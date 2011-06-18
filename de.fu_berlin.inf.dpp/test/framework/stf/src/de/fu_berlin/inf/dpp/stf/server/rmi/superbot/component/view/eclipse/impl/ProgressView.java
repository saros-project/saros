package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.Component;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;

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
                return "there still exist some progresses";
            }
        });

    }

}
