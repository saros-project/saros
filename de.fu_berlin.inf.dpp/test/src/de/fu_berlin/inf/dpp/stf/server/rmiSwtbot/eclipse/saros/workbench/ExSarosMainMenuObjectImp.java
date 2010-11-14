package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseMainMenuObjectImp;

public class ExSarosMainMenuObjectImp extends EclipseMainMenuObjectImp implements
    ExMainMenuObject {
    // public static SarosMainMenuObjectImp classVariable;

    private static transient ExSarosMainMenuObjectImp self;

    /**
     * {@link ExSarosMainMenuObjectImp} is a singleton, but inheritance is
     * possible.
     */
    public static ExSarosMainMenuObjectImp getInstance() {
        if (self != null)
            return self;
        self = new ExSarosMainMenuObjectImp();
        return self;
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        exWorkbenchO.getEclipseShell().activate().setFocus();
        menuO.clickMenuWithTexts("Saros", "Create Account");
        exWindowO.confirmCreateNewUserAccountWindow(jid.getDomain(),
            jid.getName(), password);
    }
}
