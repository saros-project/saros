package saros.session.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ActivityHandlerTest.class,
  ActivityQueuerTest.class,
  ActivitySequencerTest.class,
  SharedReferencePointMapperTest.class,
  UserInformationHandlerTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
