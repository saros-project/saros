package de.fu_berlin.inf.dpp.stf.test.partialsharing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShareFilesToProjectsWithDifferentEncodingTest extends StfTestCase {

  private static String CONTENT =
      "Bittida en morgon, innan solen upprann,\nInnan foglarna började sjunga,\nBergatrollet friade till fager ungersven.\nHon hade en falskeliger tunga:\nHerr Mannelig, herr Mannelig, trolofven I mig.\nFör det jag bjuder så gerna;\nI kunnen väl svara endast ja eller nej.\nOm i viljen eller ej.\nEder vill jag gifva de gångare tolf,\nSom gå uti rosendelunden;\nAldrig har det varit någon sadel uppå dem,\nEj heller betsel uti munnen.\nEder vill jag gifva de qvarnarna tolf,\nSom stå mellan Tillö och Ternö;\nStenarna de äro af rödaste gull,\nOch hjulen silfverbeslagna.\nEder vill jag gifva ett förgyllande svärd,\nSom klingar utaf femton guldringar;\nOch strida huru I strida vill,\nStridsplatsen skolen i väl vinna.\nEder vill jag gifva en skjorta så ny,\nDen bästa I lysten att slita;\nInte är hon sömmad av nål eller trå,\nMen virkad af silket det hvita.\nSådana gåfvor jag toge väl emot,\nOm du vore kristelig qvinna,\nMen nu så är du det värsta bergatroll\nAf Neckens och djefvulens stämma.\nBergatrollet ut på dörren sprang,\nHon rister och jämrar sig svåra:\nHade jag fått den fager ungersven,\nSå hade jag mistat min plåga.\nHerr Mannelig herr Mannelig trolofven I mig.\nFör det jag bjuder så gerna;\nI kunnen väl svara endast ja eller nej,\nOm i viljen eller ej.";

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Before
  public void setUp() throws Exception {
    closeAllShells();
    closeAllEditors();
    clearWorkspaces();
  }

  @After
  public void tearDown() throws Exception {
    leaveSessionHostFirst(ALICE);
  }

  @Test
  public void testShareFilesWithDifferentProjectEncodingsAndRecovery() throws Exception {

    createProjects("UTF-8", "UTF-16");

    Util.buildFileSessionConcurrently(
        "foo", new String[] {"Herr Mannelig.txt"}, TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/Herr Mannelig.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    ALICE.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    BOB.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    // Watchdog needs up to 10 seconds to kick in
    Thread.sleep(10000);

    // 10 seconds + 5 seconds default timeout < Watchdog update period
    // FIXME fix waitUntilIsInconsistencyDetected
    BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

    BOB.superBot().views().sarosView().resolveInconsistency();

    assertEquals(
        ALICE.remoteBot().editor("Herr Mannelig.txt").getText(),
        BOB.remoteBot().editor("Herr Mannelig.txt").getText());
  }

  // FIMXE move to another test package as this has nothing to do with partial
  // sharing
  @Test
  public void testChangeFileEncodingInFullSharedProject() throws Exception {

    /*
     * Important for full sharing: There is some "magic" involved here.
     * Invoking changeFileEncoding after sync. will also change the file
     * with the encoding settings on BOBs side which is then transmitted to
     * ALICEs side which will "corrupt" (this is actually not a bug, but a
     * dangerous operation) the file on her side too ! Even if this file is
     * not transmitted, changing the encoding will trigger a save activity
     * so ALICE gets -> garbage text apply + save activity = garbage on
     * ALICE disk !!!
     */

    createProjects("UTF-8", "UTF-8");

    Util.buildSessionSequentially("foo", TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/Herr Mannelig.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    ALICE.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    BOB.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    BOB.superBot().internal().changeFileEncoding("foo", "Herr Mannelig.txt", "US-ASCII");

    // Will not work, the text will change exactly 4 TIMES !
    // BOB.controlBot().getNetworkManipulator()
    // .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

    Thread.sleep(10000);

    // assert at least both have the same "garbage"

    assertEquals(
        ALICE.remoteBot().editor("Herr Mannelig.txt").getText(),
        BOB.remoteBot().editor("Herr Mannelig.txt").getText());
  }

  @Test
  public void testShareFilesWithDifferentFileEncodingsAndRecovery() throws Exception {

    createProjects("UTF-8", "UTF-8");

    BOB.superBot().internal().changeFileEncoding("foo", "Herr Mannelig.txt", "US-ASCII");

    Util.buildFileSessionConcurrently(
        "foo", new String[] {"Herr Mannelig.txt"}, TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/Herr Mannelig.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    ALICE.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "Herr Mannelig.txt").open();
    BOB.remoteBot().editor("Herr Mannelig.txt").waitUntilIsActive();

    // Watchdog needs up to 10 seconds to kick in
    Thread.sleep(10000);

    // 10 seconds + 5 seconds default timeout < Watchdog update period
    // FIXME fix waitUntilIsInconsistencyDetected
    BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

    BOB.superBot().views().sarosView().resolveInconsistency();

    assertEquals(CONTENT, ALICE.remoteBot().editor("Herr Mannelig.txt").getText());

    assertEquals(
        ALICE.remoteBot().editor("Herr Mannelig.txt").getText(),
        BOB.remoteBot().editor("Herr Mannelig.txt").getText());
  }

  private void createProjects(String aliceCharset, String bobCharset) throws Exception {
    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().changeProjectEncoding("foo", aliceCharset);

    BOB.superBot().internal().createProject("foo");
    BOB.superBot().internal().changeProjectEncoding("foo", bobCharset);

    ALICE.superBot().internal().createFile("foo", "Herr Mannelig.txt", CONTENT);

    ALICE.superBot().internal().createFile("foo", "dummy.txt", CONTENT);

    BOB.superBot().internal().createFile("foo", "Herr Mannelig.txt", "");
  }
}
