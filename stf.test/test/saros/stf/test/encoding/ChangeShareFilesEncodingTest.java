package saros.stf.test.encoding;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

public class ChangeShareFilesEncodingTest extends StfTestCase {

  private static String CONTENT =
      "Bittida en morgon, innan solen upprann,\n"
          + "Innan foglarna började sjunga,\n"
          + "Bergatrollet friade till fager ungersven.\n"
          + "Hon hade en falskeliger tunga:\n"
          + "Herr Mannelig, herr Mannelig, trolofven I mig.\n"
          + "För det jag bjuder så gerna;\n"
          + "I kunnen väl svara endast ja eller nej.\n"
          + "Om i viljen eller ej.\n"
          + "Eder vill jag gifva de gångare tolf,\n"
          + "Som gå uti rosendelunden;\n"
          + "Aldrig har det varit någon sadel uppå dem,\n"
          + "Ej heller betsel uti munnen.\n"
          + "Eder vill jag gifva de qvarnarna tolf,\n"
          + "Som stå mellan Tillö och Ternö;\n"
          + "Stenarna de äro af rödaste gull,\n"
          + "Och hjulen silfverbeslagna.\n"
          + "Eder vill jag gifva ett förgyllande svärd,\n"
          + "Som klingar utaf femton guldringar;\n"
          + "Och strida huru I strida vill,\n"
          + "Stridsplatsen skolen i väl vinna.\n"
          + "Eder vill jag gifva en skjorta så ny,\n"
          + "Den bästa I lysten att slita;\n"
          + "Inte är hon sömmad av nål eller trå,\n"
          + "Men virkad af silket det hvita.\n"
          + "Sådana gåfvor jag toge väl emot,\n"
          + "Om du vore kristelig qvinna,\n"
          + "Men nu så är du det värsta bergatroll\n"
          + "Af Neckens och djefvulens stämma.\n"
          + "Bergatrollet ut på dörren sprang,\n"
          + "Hon rister och jämrar sig svåra:\n"
          + "Hade jag fått den fager ungersven,\n"
          + "Så hade jag mistat min plåga.\n"
          + "Herr Mannelig herr Mannelig trolofven I mig.\n"
          + "För det jag bjuder så gerna;\n"
          + "I kunnen väl svara endast ja eller nej,\n"
          + "Om i viljen eller ej.";

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
