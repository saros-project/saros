package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AliceTestSuite.class, AliceAndBobTestSuite.class,
    AliceAndBobAndCarlTestSuite.class,
    AliceAndBobAndCarlAndDaveTestSuite.class })
public class StfTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations

}
