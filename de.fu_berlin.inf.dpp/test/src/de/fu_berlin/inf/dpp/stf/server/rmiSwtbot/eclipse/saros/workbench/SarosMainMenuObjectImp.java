package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosControler;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseMainMenuObjectImp;

public class SarosMainMenuObjectImp extends EclipseMainMenuObjectImp implements
    SarosMainMenuObject {
    public static SarosMainMenuObjectImp classVariable;

    public SarosMainMenuObjectImp(SarosControler rmiBot) {
        super(rmiBot);
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        rmiBot.workbench.getEclipseShell().activate().setFocus();
        menuObject.clickMenuWithTexts("Saros", "Create Account");
        rmiBot.exportedPopUpWindow.confirmCreateNewUserAccountWindow(
            jid.getDomain(), jid.getName(), password);
    }
}
