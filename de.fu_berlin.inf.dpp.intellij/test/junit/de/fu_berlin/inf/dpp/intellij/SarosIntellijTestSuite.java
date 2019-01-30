package de.fu_berlin.inf.dpp.intellij;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  de.fu_berlin.inf.dpp.intellij.context.TestSuite.class,
  de.fu_berlin.inf.dpp.intellij.eventhandler.editor.document.TestSuite.class,
  de.fu_berlin.inf.dpp.intellij.project.filesystem.TestSuite.class,
  de.fu_berlin.inf.dpp.intellij.ui.swt_browser.TestSuite.class,
})
public class SarosIntellijTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
