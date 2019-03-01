package saros;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.ui.browser_functions.TestSuite.class,
  saros.ui.manager.TestSuite.class,
  saros.ui.model.TestSuite.class,
  saros.HTMLUIContextFactoryTest.class
})
public class HTMLUITestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
