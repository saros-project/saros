package de.fu_berlin.inf.dpp.stf.test.permissionsandfollowmode;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.TB_INCONSISTENCY_DETECTED;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.TB_NO_INCONSISTENCIES;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.VIEW_SAROS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class WriteAccessChangeAndImmediateWriteTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(ALICE, BOB);
    }

    /**
     * Steps:
     * 
     * 1. ALICE restrict to read only access.
     * 
     * 2. BOB try to create inconsistency (set Text)
     * 
     * 3. ALICE grants write access to BOB
     * 
     * 4. BOB immediately begins to write it.
     * 
     * Expected Results:
     * 
     * 2. inconsistency should occur by BOB.
     * 
     * 4. no inconsistency occur by BOB.
     * 
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws RemoteException {

        ALICE.superBot().views().sarosView().selectParticipant(BOB.getJID())
            .restrictToReadOnlyAccess();
        BOB.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

        assertTrue(BOB.remoteBot().view(VIEW_SAROS)
            .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
            .isEnabled());
        BOB.superBot().views().sarosView().inconsistencyDetected();

        ALICE.superBot().views().sarosView().selectParticipant(BOB.getJID())
            .grantWriteAccess();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextWithoutSave(Constants.CP2);
        BOB.remoteBot().sleep(5000);
        assertFalse(BOB.remoteBot().view(VIEW_SAROS)
            .toolbarButton(TB_NO_INCONSISTENCIES).isEnabled());
    }
}
