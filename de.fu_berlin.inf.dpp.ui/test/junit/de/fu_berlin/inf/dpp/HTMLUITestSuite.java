package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  de.fu_berlin.inf.dpp.ui.browser_functions.TestSuite.class,
  de.fu_berlin.inf.dpp.ui.manager.TestSuite.class,
  de.fu_berlin.inf.dpp.ui.model.TestSuite.class,
  de.fu_berlin.inf.dpp.HTMLUIContextFactoryTest.class
})
public class HTMLUITestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
