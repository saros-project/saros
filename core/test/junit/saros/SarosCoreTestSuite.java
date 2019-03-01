package saros;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.account.TestSuite.class,
  saros.activities.TestSuite.class,
  saros.communication.extensions.TestSuite.class,
  saros.concurrent.TestSuite.class,
  saros.concurrent.jupiter.test.puzzles.TestSuite.class,
  saros.editor.colorstorage.TestSuite.class,
  saros.editor.remote.TestSuite.class,
  saros.filesystem.TestSuite.class,
  saros.misc.xstream.TestSuite.class,
  saros.monitoring.TestSuite.class,
  saros.negotiation.TestSuite.class,
  saros.net.TestSuite.class,
  saros.net.internal.TestSuite.class,
  saros.preferences.TestSuite.class,
  saros.session.TestSuite.class,
  saros.session.internal.TestSuite.class,
  saros.synchronize.TestSuite.class,
  saros.util.TestSuite.class,
  saros.versioning.TestSuite.class,
  saros.SarosCoreContextFactoryTest.class
})
public class SarosCoreTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
