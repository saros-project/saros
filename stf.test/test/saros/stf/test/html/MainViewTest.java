package saros.stf.test.html;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.ui.View.MAIN_VIEW;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfHtmlTestCase;

public class MainViewTest extends StfHtmlTestCase {
  @BeforeClass
  public static void selectTesters() throws Exception {
    selectFirst(ALICE);
  }

  @AfterClass
  public static void cleanUpSaros() throws Exception {
    tearDownSarosLast();
  }

  @Test
  public void expectedButtonsInMainView() throws Exception {
    assertTrue("Main view did not load", ALICE.htmlBot().view(MAIN_VIEW).isOpen());

    assertTrue(
        "No 'Add Contact' button", ALICE.htmlBot().view(MAIN_VIEW).hasElementWithId("add-contact"));
    assertTrue(
        "No 'Start Session' button",
        ALICE.htmlBot().view(MAIN_VIEW).hasElementWithId("start-session"));
  }
}
