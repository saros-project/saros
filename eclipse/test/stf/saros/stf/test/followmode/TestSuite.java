package saros.stf.test.followmode;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  FollowModeTest.class,
  FollowModeDisabledInNewSessionTest.class,
  RefactorInFollowModeTest.class,
  SimpleFollowModeITest.class,
  SimpleFollowModeIITest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
