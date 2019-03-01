package saros;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.concurrent.undo.TestSuite.class,
  saros.editor.internal.TestSuite.class,
  saros.feedback.TestSuite.class,
  saros.project.TestSuite.class,
  saros.session.internal.TestSuite.class,
  saros.ui.model.roster.TestSuite.class,
  saros.ui.wizards.pages.TestSuite.class,
  saros.util.UtilTestSuite.class,
  saros.SarosEclipseContextFactoryTest.class,
  saros.SarosEclipseContextTest.class
})
public class SarosEclipseTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
