package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.accountManagement.AccountManagementTestSuite;
import de.fu_berlin.inf.dpp.concurrent.ConcurrencyTestSuite;
import de.fu_berlin.inf.dpp.feedback.FeedbackTestSuite;
import de.fu_berlin.inf.dpp.project.ProjectTestSuite;
import de.fu_berlin.inf.dpp.synchronize.SynchronizeTestSuite;
import de.fu_berlin.inf.dpp.util.UtilTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AccountManagementTestSuite.class,
    ConcurrencyTestSuite.class, FeedbackTestSuite.class, UtilTestSuite.class,
    ProjectTestSuite.class, SynchronizeTestSuite.class })
public class AllTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
