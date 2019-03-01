package de.fu_berlin.inf.dpp.stf.test.stf.view.html;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import java.rmi.RemoteException;
import org.junit.BeforeClass;
import org.junit.Test;

public class HtmlViewTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE);
    assumeTrue(ALICE.usesHtmlGui());
  }

  @Test
  public void openingAndClosingSarosHTMLView() throws RemoteException {
    ALICE.htmlViewBot().closeSarosBrowserView();
    assertFalse(ALICE.htmlViewBot().isSarosBrowserViewOpen());

    ALICE.htmlViewBot().openSarosBrowserView();
    assertTrue(ALICE.htmlViewBot().isSarosBrowserViewOpen());

    ALICE.htmlViewBot().closeSarosBrowserView();
    assertFalse(ALICE.htmlViewBot().isSarosBrowserViewOpen());
  }
}
