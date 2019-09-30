package saros.intellij;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.intellij.context.TestSuite.class,
  saros.intellij.eventhandler.editor.document.TestSuite.class,
  saros.intellij.project.filesystem.TestSuite.class,
})
public class SarosIntellijTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
