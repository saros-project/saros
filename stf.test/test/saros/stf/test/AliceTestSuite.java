package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import saros.stf.test.account.AccountPreferenceTest;
import saros.stf.test.html.MainViewTest;
import saros.stf.test.invitation.ShareProjectWizardUITest;

//order of the test classes is important

@RunWith(Suite.class)
@Suite.SuiteClasses({ AccountPreferenceTest.class,
    ShareProjectWizardUITest.class, MainViewTest.class

})
public class AliceTestSuite {
    //
}
