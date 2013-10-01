package de.fu_berlin.inf.dpp.activities.business;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ChangeColorActivityTest.class, ChecksumActivityTest.class,
    ChecksumErrorActivityTest.class, EditorActivityTest.class,
    FileActivityTest.class, FolderActivityTest.class, NOPActivityTest.class,
    PermissionActivityTest.class, ProgressActivityTest.class,
    RecoveryFileActivityTest.class, StartFollowingActivityTest.class,
    StopActivityTest.class, StopFollowingActivityTest.class,
    TextSelectionActivityTest.class, VCSActivityTest.class,
    ViewportActivityTest.class, ShareConsoleActivityTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
