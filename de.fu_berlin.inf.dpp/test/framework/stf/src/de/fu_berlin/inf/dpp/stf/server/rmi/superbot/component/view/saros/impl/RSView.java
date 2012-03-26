package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IRSView;

public final class RSView extends StfRemoteObject implements IRSView {

    private static final RSView INSTANCE = new RSView();

    private SWTBotView view;

    public static RSView getInstance() {
        return INSTANCE;
    }

    public IRSView setView(SWTBotView view) {
        this.view = view;
        return this;
    }

    public void clickTBChangeModeOfImageSource() throws RemoteException {
        view.toolbarButton(TB_CHANGE_MODE_IMAGE_SOURCE).click();
    }

    public void clickTBStopRunningSession() throws RemoteException {
        view.toolbarButton(TB_STOP_RUNNING_SESSION).click();
    }

    public void clickTBResume() throws RemoteException {
        view.toolbarButton(TB_RESUME).click();
    }

    public void clickTBPause() throws RemoteException {
        view.toolbarButton(TB_PAUSE).click();
    }
}
