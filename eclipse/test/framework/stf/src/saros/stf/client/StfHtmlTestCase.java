package saros.stf.client;

import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import saros.stf.client.tester.AbstractTester;

/**
 * Special type of {@link StfTestCase} which makes sure that all {@linkplain #select(AbstractTester,
 * AbstractTester...) selected testers} actually support the HTML GUI before the tests get executed.
 * Without HTML GUIs enabled, the test cases will be simply ignored.
 */
public abstract class StfHtmlTestCase extends StfTestCase {
  @Before
  public void prepareHtmlGui() throws Exception {
    for (AbstractTester tester : getCurrentTesters()) {
      assumeTrue(tester.usesHtmlGui());
    }

    for (AbstractTester tester : getCurrentTesters()) {
      tester.htmlViewBot().closeSarosBrowserView();
      tester.htmlViewBot().openSarosBrowserView();
    }
  }
}
