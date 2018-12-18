package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import java.util.regex.Pattern;
import org.junit.BeforeClass;
import org.junit.Test;

public class EditFileThatIsNotOpenOnRemoteSideTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  /*
   * So what is going wrong here:
   *
   * Bob edits the file, Alice editor gets dirty, even it is not open. We are
   * then forcing a save and that is where things goes wrong.
   *
   * Since the editor is not open, the SharedResourceManager thinks that the
   * save on Alice side generated a new file (BUG 1: Solution if Bob edits a
   * file that is not open on Alice side, open the editor too and switch back
   * to the editor Alice was working on)
   *
   * The file is now send to BOB (NETWORK USAGE !!!) and is replaced (BUG 2)
   * just directly on the file system, without using the document provider
   * (this causing this test to fail).
   *
   * If you put out the assertStatement you would see that somehow the text
   * will be correct if this test case ends.
   */
  @Test
  public void testEditFileThatIsNotOpenOnRemoteSide() throws Exception {

    Pattern pattern = Pattern.compile("b*+(a|\\n)++");
    String content;

    StringBuilder builder = new StringBuilder(512);

    for (int i = 0; i < 10; i++)
      builder.append(
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n");

    content = builder.toString();

    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFile("foo", "text.txt", content);

    Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/text.txt");

    // uncomment to let the test case pass
    // ALICE.superBot().views().packageExplorerView()
    // .selectFile("foo", "text.txt").open();
    //
    // ALICE.remoteBot().editor("text.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "text.txt").open();

    BOB.remoteBot().editor("text.txt").waitUntilIsActive();

    IRemoteBotEditor editor = BOB.remoteBot().editor("text.txt");

    for (int i = 0; i < 20; i++) {
      String text = editor.getText();
      assertTrue(
          "text corrupted: '" + text + "' does not match regex: b*+(a|\\n)++",
          pattern.matcher(text).matches());
      editor.typeText("b");
      editor.save();
    }
  }
}
