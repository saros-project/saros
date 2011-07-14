package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;

public final class ProgressView extends StfRemoteObject implements
    IProgressView {

    private static final Logger log = Logger.getLogger(ProgressView.class);
    private static final ProgressView INSTANCE = new ProgressView();

    private IRemoteBotView view;

    public static ProgressView getInstance() {
        return INSTANCE;
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

    public boolean existsProgress() throws RemoteException {
        try {
            view.toolbarButton(TB_REMOVE_ALL_FINISHED_OPERATIONS).click();
            return view.bot().existsToolbarButton();
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return false;
        }
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilNotExistsProgress() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !existsProgress();
            }

            public String getFailureMessage() {
                return "there still exist some progresses";
            }
        });

    }

}
