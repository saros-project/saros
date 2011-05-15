package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestAllParticipantsFollowUserWithWriteAccess extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * <li>All read-only users enable followmode</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl, dave);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException,
        InterruptedException {
        setFollowMode(alice, bob, carl, dave);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice open a editor</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob, carl and dave's editor would be opened too.</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void followingUserOpenClassWhenFollowedUserOpenClass()
        throws RemoteException, InterruptedException {
        alice.remoteBot().editor(CLS1_SUFFIX).closeWithSave();
        bob.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        carl.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        dave.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(bob.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(carl.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(dave.remoteBot().isEditorOpen(CLS1_SUFFIX));

        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        // setFollowMode(alice, bob, carl, dave);

        bob.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);
        carl.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);
        dave.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);

        assertTrue(bob.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(carl.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(dave.remoteBot().isEditorOpen(CLS1_SUFFIX));

    }

    /**
     * Steps:
     * <ol>
     * <li>Alice open a editor and set text in the opened java editor without
     * save.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob, carl and dave have the same dirty content as alice and their
     * editor would be opened and activated too.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testFollowModeByEditingClassByAlice() throws RemoteException {
        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyClsContentOfAlice = alice.remoteBot().editor(CLS1_SUFFIX)
            .getText();

        bob.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        carl.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        dave.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));
        bob.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        carl.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        dave.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice open a class.</li>
     * <li>Alice set text in the opened java editor and then close it with save.
     * </li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>bob, carl and dave open the class too</li>
     * <li>bob, carl and dave have the same class content as alice and their
     * opened editor are closed too.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testFollowModeByClosingEditorByAlice() throws IOException,
        CoreException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);
        carl.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);
        dave.remoteBot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(carl.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(dave.remoteBot().isEditorOpen(CLS1_SUFFIX));

        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1_CHANGE);
        alice.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        String clsContentOfAlice = alice.superBot().views()
            .packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1));

        bob.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        carl.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        dave.remoteBot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(bob.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(carl.remoteBot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(dave.remoteBot().isEditorOpen(CLS1_SUFFIX));

        assertTrue(bob.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
        assertTrue(carl.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
        assertTrue(bob.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
    }
}
