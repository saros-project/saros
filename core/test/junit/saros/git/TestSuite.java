package saros.git;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({JGitFacadeTest.class, JGitFacadeMergeTest.class, GitManagerTest.class})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
