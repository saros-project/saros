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
     */
    @Test
    public void followingUserOpenClassWhenFollowedUserOpenClass()
        throws RemoteException {
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        bob.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        carl.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        dave.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(carl.bot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(dave.bot().isEditorOpen(CLS1_SUFFIX));

        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).contextMenu(CM_OPEN).click();

        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        carl.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        dave.bot().waitUntilEditorOpen(CLS1_SUFFIX);

        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(carl.bot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(dave.bot().isEditorOpen(CLS1_SUFFIX));

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
        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyClsContentOfAlice = alice.bot().editor(CLS1_SUFFIX)
            .getText();

        bob.bot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(bob.bot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        carl.bot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(carl.bot().editor(CLS1_SUFFIX).isActive());
        assertTrue(carl.bot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(carl.bot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        dave.bot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(dave.bot().editor(CLS1_SUFFIX).isActive());
        assertTrue(dave.bot().editor(CLS1_SUFFIX).isDirty());
        assertTrue(dave.bot().editor(CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));
        bob.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        carl.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        dave.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
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
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).contextMenu(CM_OPEN).click();
        bob.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        carl.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        dave.bot().waitUntilEditorOpen(CLS1_SUFFIX);
        assertTrue(bob.bot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(carl.bot().isEditorOpen(CLS1_SUFFIX));
        assertTrue(dave.bot().isEditorOpen(CLS1_SUFFIX));

        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1_CHANGE);
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
        String clsContentOfAlice = alice.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));

        bob.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        carl.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        dave.bot().waitUntilEditorClosed(CLS1_SUFFIX);
        assertFalse(bob.bot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(carl.bot().isEditorOpen(CLS1_SUFFIX));
        assertFalse(dave.bot().isEditorOpen(CLS1_SUFFIX));

        assertTrue(bob.noBot()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
        assertTrue(carl.noBot()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
        assertTrue(bob.noBot()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1))
            .equals(clsContentOfAlice));
    }
}
