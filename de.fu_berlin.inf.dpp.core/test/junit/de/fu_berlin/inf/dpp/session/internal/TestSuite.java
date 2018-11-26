package de.fu_berlin.inf.dpp.session.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ActivityHandlerTest.class,
  ActivityQueuerTest.class,
  ActivitySequencerTest.class,
  SharedProjectMapperTest.class,
  UserInformationHandlerTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
