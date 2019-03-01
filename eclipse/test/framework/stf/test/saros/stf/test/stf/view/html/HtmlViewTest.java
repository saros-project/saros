package saros.stf.test.stf.view.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static saros.stf.client.tester.SarosTester.ALICE;

import java.rmi.RemoteException;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;

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
