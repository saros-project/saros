package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.EclipseMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;

public class SarosMainMenuObject extends EclipseMainMenuObject implements
    ISarosMainMenuObject {
    public static SarosMainMenuObject classVariable;

    public SarosMainMenuObject(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public void creatNewAccount(JID jid, String password)
        throws RemoteException {
        rmiBot.getEclipseShell().activate().setFocus();
        menuObject.clickMenuWithTexts("Saros", "Create Account");
        rmiBot.popupWindowObject.confirmCreateNewUserAccountWindow(
            jid.getDomain(), jid.getName(), password);
    }
}
