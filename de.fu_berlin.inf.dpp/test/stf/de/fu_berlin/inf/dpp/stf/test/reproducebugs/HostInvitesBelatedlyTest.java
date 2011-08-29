package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class HostInvitesBelatedlyTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>All read-only users enable follow mode</li>
     * </ol>
     * 
     */

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    /**
     * Steps:
     * 
     * 1. ALICE edits the file CLS but don't saves it.
     * 
     * 2. BOB edits the file CLS in the project currently used and saves it.
     * 
     * 3. ALICE edits the file CLS2 but don't saves it.
     * 
     * 4. BOB edits the file CLS2 in the project currently used and don't saves
     * it.
     * 
     * 5. ALICE invites BOB.
     * 
     * 6. The question about the changed files at BOB is answered with YES.
     * 
     * 
     * Expected Results:
     * 
     * 7. BOB has the same project like host.
     * 
     * FIXME: There are some bugs, if BOB's editors are not closed, BOB has the
     * different project like host.
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws Exception,
        CoreException, InterruptedException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1, Constants.CLS2);
        BOB.superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1, Constants.CLS2);

        /*
         * ALICE build session only with CARL and is followed by CARL.
         */
        Util.buildSessionConcurrently(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, CARL);
        Util.activateFollowMode(ALICE, CARL);

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).setText(Constants.CP1);
        String dirtyContent1ByAlice = ALICE.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();

        BOB.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP1_CHANGE);

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();

        ALICE.remoteBot().editor(Constants.CLS2_SUFFIX).setText(Constants.CP2);
        String dirtyContent2ByAlice = ALICE.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();

        BOB.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();

        BOB.remoteBot().editor(Constants.CLS2_SUFFIX)
            .setText(Constants.CP2_CHANGE);
        // BOB.editor.closeJavaEditorWithSave(CLS1);
        // BOB.editor.closeJavaEditorWithSave(CLS2);

        Util.inviteBuddies(Constants.PROJECT1,
            TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyContent1ByAlice);
        BOB.remoteBot().editor(Constants.CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyContent2ByAlice);

        String CLSContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();
        String CLS2ContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();

        String CLSContentOfBob = BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .getText();
        String CLS2ContentOfBob = BOB.remoteBot().editor(Constants.CLS2_SUFFIX)
            .getText();

        assertEquals(CLSContentOfAlice, CLSContentOfBob);
        assertEquals(CLS2ContentOfAlice, CLS2ContentOfBob);
    }
}
