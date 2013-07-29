package de.fu_berlin.inf.dpp.activities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivityTest;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivityTest;
import de.fu_berlin.inf.dpp.activities.business.ChecksumErrorActivityTest;
import de.fu_berlin.inf.dpp.activities.business.EditorActivityTest;
import de.fu_berlin.inf.dpp.activities.business.FileActivityTest;
import de.fu_berlin.inf.dpp.activities.business.FolderActivityTest;
import de.fu_berlin.inf.dpp.activities.business.NOPActivityTest;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivityTest;
import de.fu_berlin.inf.dpp.activities.business.ProgressActivityTest;
import de.fu_berlin.inf.dpp.activities.business.RecoveryFileActivityTest;
import de.fu_berlin.inf.dpp.activities.business.StartFollowingActivityTest;
import de.fu_berlin.inf.dpp.activities.business.StopActivityTest;
import de.fu_berlin.inf.dpp.activities.business.StopFollowingActivityTest;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivityTest;
import de.fu_berlin.inf.dpp.activities.business.VCSActivityTest;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivityTest;

@RunWith(Suite.class)
@SuiteClasses({ ChangeColorActivityTest.class, ChecksumActivityTest.class,
    ChecksumErrorActivityTest.class, EditorActivityTest.class,
    FileActivityTest.class, FolderActivityTest.class, NOPActivityTest.class,
    PermissionActivityTest.class, ProgressActivityTest.class,
    RecoveryFileActivityTest.class, StartFollowingActivityTest.class,
    StopActivityTest.class, StopFollowingActivityTest.class,
    TextSelectionActivityTest.class, VCSActivityTest.class,
    ViewportActivityTest.class })
public class ActivityTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
