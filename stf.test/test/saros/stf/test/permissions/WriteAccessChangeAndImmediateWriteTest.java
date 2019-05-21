package saros.stf.test.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.shared.Constants.TB_INCONSISTENCY_DETECTED;
import static saros.stf.shared.Constants.TB_NO_INCONSISTENCIES;
import static saros.stf.shared.Constants.VIEW_SAROS;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.test.stf.Constants;

public class WriteAccessChangeAndImmediateWriteTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
        restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB);
    }

    @Before
    public void setUp() throws Exception {

        closeAllShells();
        closeAllEditors();

    }

    @After
    public void cleanUpSaros() throws Exception {

        tearDownSarosLast();

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
    public void testFollowModeByOpenClassbyAlice() throws Exception {

        ALICE.superBot().internal().createJavaClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1);
        BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared(
            Util.classPathToFilePath(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1));

        ALICE.superBot().views().sarosView().selectUser(BOB.getJID())
            .restrictToReadOnlyAccess();
        ALICE.superBot().views().sarosView().selectUser(BOB.getJID())
            .waitUntilHasReadOnlyAccess();
        BOB.superBot().views().packageExplorerView()
            .selectFile("Foo1_Saros", "src", "my", "pkg", "MyClass.java")
            .open();

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP1);
        BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

        assertTrue(BOB.remoteBot().view(VIEW_SAROS)
            .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
            .isEnabled());
        BOB.superBot().views().sarosView().resolveInconsistency();

        ALICE.superBot().views().sarosView().selectUser(BOB.getJID())
            .grantWriteAccess();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP2);

        Thread.sleep(5000);

        assertFalse(BOB.remoteBot().view(VIEW_SAROS)
            .toolbarButton(TB_NO_INCONSISTENCIES).isEnabled());

    }
}
