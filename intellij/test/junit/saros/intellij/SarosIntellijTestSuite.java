package saros.intellij;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import saros.intellij.filesystem.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.intellij.context.TestSuite.class,
  saros.intellij.editor.TestSuite.class,
  saros.intellij.eventhandler.editor.document.TestSuite.class,
  TestSuite.class,
})
public class SarosIntellijTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
