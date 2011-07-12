package de.fu_berlin.inf.dpp.stf.test.rosterviewbehaviour;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

@TestLink(id = "Saros-39_changing_names_in_roster_view")
public class ChangingNameInRosterViewTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void renameBuddyInRosterView() throws RemoteException {

        Util.setUpSessionWithJavaProjectAndClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));

        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename(BOB.getName());

        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));

        assertTrue(ALICE.superBot().views().sarosView()
            .getNickname(BOB.getJID()).equals(BOB.getName()));

        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename("new BOB");

        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));

        assertTrue(ALICE.superBot().views().sarosView()
            .getNickname(BOB.getJID()).equals("new BOB"));

    }

}
