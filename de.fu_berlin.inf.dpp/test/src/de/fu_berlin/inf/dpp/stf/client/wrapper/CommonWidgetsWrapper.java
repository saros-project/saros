package de.fu_berlin.inf.dpp.stf.client.wrapper;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.View;

public class CommonWidgetsWrapper extends Wrapper {

    public CommonWidgetsWrapper(Tester tester) {
        super(tester);
        // TODO Auto-generated constructor stub
    }

    public View view(String viewTitle) throws RemoteException {
        tester.view.setViewTitle(viewTitle);
        return tester.view;
    }
}
