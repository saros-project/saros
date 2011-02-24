package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.editor;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertDoesNotContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestEditorByAlice extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void testTypeInEditor() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        String fileName = "test.txt";
        String[] path = { PROJECT1, fileName };
        alice.sarosBot().file().newFile(path);
        alice.bot().editor(fileName).waitUntilIsActive();

        String expected = "Hello World";
        alice.bot().editor(fileName).typeText(expected);
        assertEquals(expected, alice.bot().editor(fileName).getText());
    }

    @Test
    public void testDeleteInEditor() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView().open()
            .openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        String fileName = CLS1 + ".java";
        alice.bot().editor(fileName).navigateTo(3, 0);
        alice.bot().editor(CLS1_SUFFIX).typeText("testtext");
        alice.bot().editor(fileName).navigateTo(3, 3);
        alice.bot().editor(fileName)
            .pressShortcut(IKeyLookup.DELETE_NAME, IKeyLookup.DELETE_NAME);
        assertEquals("tesext", alice.bot().editor(CLS1_SUFFIX).getTextOnLine(3));
    }

    @Test
    public void testEnterInEditor() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView().open()
            .openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        String fileName = CLS1 + ".java";
        alice.bot().editor(fileName).navigateTo(3, 0);
        alice.bot().editor(CLS1_SUFFIX).typeText("testtext");

        alice.bot().editor(fileName).pressShortCutEnter();
        assertEquals(4, alice.bot().editor(CLS1_SUFFIX).getCursorLine());
    }

    @Test
    public void autoComplateProposal() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView().open()
            .openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        alice.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        assertDoesNotContain("public static void main",
            alice.bot().editor(CLS1_SUFFIX).getTextOnLine(3));
        alice.bot().editor(CLS1_SUFFIX)
            .autoCompleteProposal("main", "main - main method");
        assertContains("public static void main",
            alice.bot().editor(CLS1_SUFFIX).getTextOnLine(3));
    }

    @Test
    public void getAutoComplateProposal() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView().open()
            .openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        alice.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        List<String> autoCompleteProposals = alice.bot().editor(CLS1_SUFFIX)
            .getAutoCompleteProposals("JFr");
        assertEquals(autoCompleteProposals.toString(), 2,
            autoCompleteProposals.size());
        assertEquals("JFrame - javax.swing", autoCompleteProposals.get(0));
        String string = autoCompleteProposals.get(1);
        assertTrue(string.equals("JFr()  void - Method stub")
            || string.equals("JFr() : void - Method stub"));
    }

    @Test
    public void pressShortCutSave() throws RemoteException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView().open()
            .openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        alice.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        alice.bot().editor(CLS1_SUFFIX)
            .autoCompleteProposal("main", "main - main method");
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isDirty());
        alice.bot().editor(CLS1_SUFFIX).pressShortCutSave();
        assertFalse(alice.bot().editor(CLS1_SUFFIX).isDirty());
    }

    @Test
    public void quickFix() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file()
            .newClassImplementsRunnable(PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).pressShortCutNextAnnotation();
        alice.bot().editor(CLS1_SUFFIX).quickfix("Add unimplemented methods");
        assertContains("public void run()", alice.bot().editor(CLS1_SUFFIX)
            .getTextOnLine(5));

        alice.bot().editor(CLS1_SUFFIX).navigateTo(7, 0);
        alice.bot().editor(CLS1_SUFFIX)
            .autoCompleteProposal("sys", "sysout - print to standard out");
    }

    @Test
    public void getSelection() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        alice.sarosBot().file().newFile(PROJECT1, FILE1);
        alice.bot().editor(FILE1).navigateTo(0, 0);
        alice.bot().editor(FILE1).typeText("pleese");
        alice.bot().editor(FILE1).selectRange(0, 0, 6);
        System.out.println(alice.bot().editor(FILE1).getSelection());

    }

    @Test
    public void quickFixWithSpellChecker() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        alice.sarosBot().file().newFile(PROJECT1, FILE1);
        alice.bot().editor(FILE1).typeText("pleese open the window");
        alice.bot().editor(FILE1).selectLine(0);
        alice.bot().editor(FILE1).quickfix(0);
        System.out.println(alice.bot().editor(FILE1).getTextOnLine(0));
        assertContains("please", alice.bot().editor(FILE1).getTextOnLine(0));
    }

    @Test
    public void allTogether() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file()
            .newClassImplementsRunnable(PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).pressShortCutNextAnnotation();
        alice.bot().editor(CLS1_SUFFIX).quickfix("Add unimplemented methods");
        assertContains("public void run()", alice.bot().editor(CLS1_SUFFIX)
            .getTextOnLine(5));
        alice.bot().editor(CLS1_SUFFIX).navigateTo(7, 0);
        alice.bot().editor(CLS1_SUFFIX)
            .autoCompleteProposal("sys", "sysout - print to standard out");
        assertContains("System.out.println()", alice.bot().editor(CLS1_SUFFIX)
            .getTextOnLine(7));
        alice.bot().editor(CLS1_SUFFIX).typeText("\"Hello World\"");
        assertContains("System.out.println(\"Hello World\")", alice.bot()
            .editor(CLS1_SUFFIX).getTextOnLine(7));

        alice.bot().editor(CLS1_SUFFIX).navigateTo(3, 0);
        alice.bot().editor(CLS1_SUFFIX)
            .autoCompleteProposal("main", "main - main method");
        alice.bot().editor(CLS1_SUFFIX)
            .typeText("new Thread (new " + CLS1 + " ()");
        alice.bot().editor(CLS1_SUFFIX)
            .pressShortCutQuickAssignToLocalVariable();
        assertContains("Thread thread = new Thread (new " + CLS1 + " ());",
            alice.bot().editor(CLS1_SUFFIX).getTextOnCurrentLine());
        alice.bot().editor(CLS1_SUFFIX).pressShortCut(SWT.NONE, '\n');
        alice.bot().editor(CLS1_SUFFIX).typeText("\n");
        assertEquals(5, alice.bot().editor(CLS1_SUFFIX).getCursorLine());
        alice.bot().editor(CLS1_SUFFIX).typeText("thread.start(");
        alice.bot().editor(CLS1_SUFFIX).typeText("\n");
        alice.bot().editor(CLS1_SUFFIX).typeText(";\n");
        alice.bot().editor(CLS1_SUFFIX).typeText("thread.join(");
        alice.bot().editor(CLS1_SUFFIX).typeText("\n");
        alice.bot().editor(CLS1_SUFFIX).typeText(";");
        alice.bot().editor(CLS1_SUFFIX).quickfix("Add throws declaration");
        assertContains("InterruptedException", alice.bot().editor(CLS1_SUFFIX)
            .getTextOnLine(3));
        alice.bot().editor(CLS1_SUFFIX).pressShortCut(SWT.NONE, '\n');
        alice.bot().editor(CLS1_SUFFIX).pressShortCutSave();
        assertFalse(alice.bot().editor(CLS1_SUFFIX).isDirty());
        alice.bot().sleep(100);
        alice.bot().editor(CLS1_SUFFIX).pressShortRunAsJavaApplication();
        alice.sarosBot().consoleView().waitUntilTextInViewConsoleExists();
        assertContains("Hello World", alice.sarosBot().consoleView()
            .getTextInConsole());

    }
}
