package de.fu_berlin.inf.dpp.stf.test.rosterviewbehaviour;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class ChangingNameInRosterViewTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice share a java project with BOB</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        // resetBuddiesName();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        // if (ALICE.sarosBot().views().buddiesView()
        // .hasBuddyNickNameNoGUI(BOB.jid)) {
        // ALICE.sarosBot().views().buddiesView()
        // .renameBuddy(BOB.jid, BOB.jid.getBase());
        // }
        resetBuddiesName();
        if (!ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID())) {
            Util.addBuddies(ALICE, BOB);
        }
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE rename BOB to "BOB_stf".</li>
     * <li>ALICE rename BOB to "new BOB".</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE hat contact with BOB and BOB'name is changed.</li>
     * <li>ALICE hat contact with BOB and BOB'name is changed.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void renameBuddyInRosterView() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename(BOB.getName());
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(ALICE.superBot().views().sarosView()
            .getNickName(BOB.getJID()).equals(BOB.getName()));
        // assertTrue(ALICE.sessionV.isContactInSessionView(BOB.getJID()));
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename("new BOB");
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(ALICE.superBot().views().sarosView()
            .getNickName(BOB.getJID()).equals("new BOB"));
        // assertTrue(ALICE.sessionV.isContactInSessionView(BOB.getJID()));
    }

}
