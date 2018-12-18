package de.fu_berlin.inf.dpp.stf.test.stf.editor;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertContains;
import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertDoesNotContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.stf.Constants;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EditorByAliceTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Before
  public void beforeEveryTest() throws Exception {
    closeAllEditors();
    clearWorkspaces();
  }

  @Test
  public void testTypeInEditor() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    String fileName = "test.txt";

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .newC()
        .file(fileName);
    ALICE.remoteBot().editor(fileName).waitUntilIsActive();

    String expected = "Hello World";
    ALICE.remoteBot().editor(fileName).typeText(expected);
    assertEquals(expected, ALICE.remoteBot().editor(fileName).getText());
  }

  @Test
  public void testDeleteInEditor() throws Exception {

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(3, 0);
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("testtext");
    Thread.sleep(100);
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(3, 4);

    for (int i = 0; i < 4; i++) {
      ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).pressShortcut(IKeyLookup.BACKSPACE_NAME);
    }

    assertEquals("text", ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getTextOnLine(3));
  }

  @Test
  public void testEnterInEditor() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
    String fileName = Constants.CLS1 + ".java";
    ALICE.remoteBot().editor(fileName).navigateTo(3, 0);
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).typeText("testtext");

    ALICE.remoteBot().editor(fileName).pressShortCutEnter();
    assertEquals(4, ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getCursorLine());
  }

  @Test
  public void autoComplateProposal() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(3, 0);
    assertDoesNotContain(
        "public static void main",
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getTextOnLine(3));

    ALICE
        .remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .autoCompleteProposal("main", "main - main method");
    assertContains(
        "public static void main",
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getTextOnLine(3));
  }

  @Test
  public void getAutoComplateProposal() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(3, 0);
    List<String> autoCompleteProposals =
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getAutoCompleteProposals("JFr");
    assertEquals(autoCompleteProposals.toString(), 2, autoCompleteProposals.size());
    assertEquals("JFrame - javax.swing", autoCompleteProposals.get(0));
    String string = autoCompleteProposals.get(1);
    assertTrue(
        string.equals("JFr()  void - Method stub") || string.equals("JFr() : void - Method stub"));
  }

  @Test
  public void pressShortCutSave() throws RemoteException {
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
        .open();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(3, 0);
    ALICE
        .remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .autoCompleteProposal("main", "main - main method");
    assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).pressShortCutSave();
    assertFalse(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
  }

  @Test
  public void quickFix() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().javaProject(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .pkg(Constants.PROJECT1, Constants.PKG1);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectPkg(Constants.PROJECT1, Constants.PKG1)
        .newC()
        .clsImplementsRunnable(Constants.CLS1);

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).selectLine(2);

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).pressShortCutNextAnnotation();

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).quickfix("Add unimplemented methods");
    assertContains(
        "public void run()", ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).getTextOnLine(5));

    ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).navigateTo(7, 0);
    ALICE
        .remoteBot()
        .editor(Constants.CLS1_SUFFIX)
        .autoCompleteProposal("sys", "sysout - print to standard out");
  }

  @Test
  public void getSelection() throws RemoteException {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .newC()
        .file(Constants.FILE1);
    ALICE.remoteBot().editor(Constants.FILE1).navigateTo(0, 0);
    ALICE.remoteBot().editor(Constants.FILE1).typeText("pleese");
    ALICE.remoteBot().editor(Constants.FILE1).selectRange(0, 0, 6);
    System.out.println(ALICE.remoteBot().editor(Constants.FILE1).getSelection());
  }

  @Test
  public void quickFixWithSpellChecker() throws Exception {
    ALICE.superBot().views().packageExplorerView().tree().newC().project(Constants.PROJECT1);
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectProject(Constants.PROJECT1)
        .newC()
        .file("readme.txt");
    ALICE.remoteBot().editor("readme.txt").typeText("pleese open the window");

    ALICE.remoteBot().editor("readme.txt").navigateTo(0, 0);

    // just wait so that eclipse recognize the spelling error
    Thread.sleep(3000);
    ALICE.remoteBot().editor("readme.txt").quickfix(0);

    assertContains("please", ALICE.remoteBot().editor("readme.txt").getTextOnLine(0));
  }
}
