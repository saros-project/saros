package saros.stf.test.stf.keyboard;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;

public class KeyboardLayoutTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
  }

  @Test
  public void testKeyboardLayout() throws Exception {

    Util.createProjectWithEmptyFile("keyboard", "text.txt", ALICE);
    //
    final String textToTest =
        "!\"§$%&/()={[]}\\+*~#'-_.:,;|<>^? abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    ALICE.superBot().views().packageExplorerView().selectFile("keyboard", "text.txt").open();

    ALICE.remoteBot().editor("text.txt").typeText(textToTest);

    assertEquals(
        "keyboard layout is misconfigured",
        textToTest,
        ALICE.remoteBot().editor("text.txt").getText());
  }
}
