package de.fu_berlin.inf.dpp.stf.test.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.ui.View.MAIN_VIEW;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfHtmlTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainViewTest extends StfHtmlTestCase {
  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
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
