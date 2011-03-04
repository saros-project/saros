package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.multiDriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestConcurrentEditing extends STFTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Before
    public void beforeEachMethod() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    static final String FILE = "file.txt";

    /**
     * Test to reproduce bug
     * "Inconsistency when concurrently writing at same position"
     * 
     * @throws RemoteException
     * @throws InterruptedException
     * 
     * @see <a
     *      href="https://sourceforge.net/tracker/?func=detail&aid=3098992&group_id=167540&atid=843359">Bug
     *      tracker entry 3098992</a>
     */
    @Test
    public void testBugInconsistencyConcurrentEditing() throws RemoteException,
        InterruptedException {
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        // cool trick, no need to always use PROJECT1, PKG1, CLS1 as arguments

        alice.sarosBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().file(FILE);
        alice.bot().waitUntilEditorOpen(FILE);
        alice.bot().editor(FILE).setTexWithSave("test/STF/lorem.txt");
        alice.bot().editor(FILE).navigateTo(0, 6);

        buildSessionSequentially(PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        bob.sarosBot().views().packageExplorerView().selectFile(path).open();

        bob.bot().waitUntilEditorOpen(FILE);
        bob.bot().editor(FILE).navigateTo(0, 30);

        bob.bot().sleep(1000);

        // Alice goes to 0,6 and hits Delete
        alice.bot().activateWorkbench();
        int waitActivate = 100;
        alice.bot().sleep(waitActivate);
        alice.bot().editor(FILE).show();

        alice.bot().editor(FILE).waitUntilIsActive();
        alice.bot().editor(FILE).pressShortcut("DELETE");
        // at the same time, Bob enters L at 0,30
        bob.bot().activateWorkbench();
        bob.bot().sleep(waitActivate);
        bob.bot().editor(FILE).show();
        bob.bot().editor(FILE).waitUntilIsActive();
        bob.bot().editor(FILE).typeText("L");
        // both sleep for less than 1000ms
        alice.bot().sleep(300);

        // Alice hits Delete again
        alice.bot().activateWorkbench();
        alice.bot().sleep(waitActivate);
        alice.bot().editor(FILE).show();
        alice.bot().editor(FILE).waitUntilIsActive();
        alice.bot().editor(FILE).pressShortcut("DELETE");
        // Bob enters o
        bob.bot().activateWorkbench();
        bob.bot().sleep(waitActivate);
        bob.bot().editor(FILE).show();
        bob.bot().editor(FILE).waitUntilIsActive();
        bob.bot().editor(FILE).typeText("o");

        alice.bot().sleep(5000);
        String aliceText = alice.bot().editor(FILE).getText();
        String bobText = bob.bot().editor(FILE).getText();
        assertEquals(aliceText, bobText);
    }

    @Test(expected = AssertionError.class)
    public void AliceAndBobeditInSameLine() throws RemoteException,
        InterruptedException {

        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionConcurrently(PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        bob.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.bot().editor(CLS1_SUFFIX).waitUntilIsActive();

        alice.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        bob.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        char[] content = "Merry Christmas and Happy New Year!".toCharArray();
        for (int i = 0; i < content.length; i++) {
            alice.bot().editor(CLS1_SUFFIX).typeText(content[i] + "");
            Thread.sleep(100);
            if (i != 0 && i % 2 == 0) {
                bob.bot().editor(CLS1_SUFFIX).navigateTo(3, i);
                bob.bot()
                    .editor(CLS1_SUFFIX)
                    .pressShortcut(IKeyLookup.DELETE_NAME,
                        IKeyLookup.DELETE_NAME);
            }
        }

        String aliceText = alice.bot().editor(CLS1_SUFFIX).getText();
        String bobText = bob.bot().editor(CLS1_SUFFIX).getText();
        System.out.println(aliceText);
        System.out.println(bobText);
        assertEquals(aliceText, bobText);
        bob.bot().sleep(5000);
        assertTrue(bob.bot().view(VIEW_SAROS_SESSION)
            .existsToolbarButton(TB_INCONSISTENCY_DETECTED));

    }
}
