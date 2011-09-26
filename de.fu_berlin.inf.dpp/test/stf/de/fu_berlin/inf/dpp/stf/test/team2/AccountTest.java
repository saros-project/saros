package de.fu_berlin.inf.dpp.stf.test.team2;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public class AccountTest extends StfTestCase implements Constants {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE);
    }

    @After
    public void runAfterEveryTest() throws Exception {
        resetDefaultAccount();
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE adds 2 different accounts</li>
     * <li>ALICE tries to activate an account, but did not choose one</li>
     * <li>ALICE activates second new created account</li>
     * <li>ALICE activates the default account</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE connects with the default account</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testActivateAccount() throws RemoteException {

        JID j1 = new JID("foo1@bar.com");
        JID j2 = new JID("foo2@bar.com");
        ISarosPreferences pref = ALICE.superBot().menuBar().saros()
            .preferences();

        // add Accounts
        pref.addAccount(j1, "j1");
        assertTrue(pref.existsAccount(j1));

        pref.addAccount(j2, "j2");
        assertTrue(pref.existsAccount(j2));

        IRemoteWorkbenchBot b = ALICE.remoteBot();
        b.menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
        b.waitUntilShellIsOpen(SHELL_PREFERNCES);

        // activate Account without choosing one
        b.buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        b.waitUntilShellIsOpen("No account selected");
        b.button(OK).click();

        // J1 aktive via SuperBot
        pref.activateAccount(j1);

        // Preferences
        b.menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
        b.waitUntilShellIsOpen(SHELL_PREFERNCES);

        // activate J2 via GUI
        b.listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE).select(
            j2.getBase());
        b.buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();

        // Label should show the new address
        String active = b
            .labelInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE).getText()
            .substring("Active:".length()).trim();
        assertTrue(active.equals("foo2@bar.com"));

        // ALICE connects
        ALICE.superBot().views().sarosView()
            .connectWith(ALICE.getJID(), ALICE.getPassword());

        // Connection status shows the right address
        try {
            ALICE.remoteBot().activeView().bot()
                .clabel(ALICE.getJID().getBase());
        } catch (WidgetNotFoundException e) {
            ALICE.remoteBot().activeView().bot()
                .clabel(ALICE.getJID().getBase().toLowerCase());
            System.out
                .println("AccountTest: Connection Status Label shows only lowercase of the chosen account."
                    + "The upcase letters were switched.");
        }
    }
}
