package saros.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({JIDTest.class, RosterTrackerTest.class, UPnPTest.class})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
