package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import saros.stf.test.chatview.ChatViewFunctionsTest;
import saros.stf.test.consistency.AddMultipleFilesTest;
import saros.stf.test.consistency.ModifyFileWithoutEditorTest;
import saros.stf.test.consistency.RecoveryWhileTypingTest;
import saros.stf.test.editing.ConcurrentEditingTest;
import saros.stf.test.editing.EditDifferentFilesTest;
import saros.stf.test.editing.EditWithReadAccessOnlyTest;
import saros.stf.test.editing.Editing3ProjectsTest;
import saros.stf.test.filefolderoperations.FolderOperationsTest;
import saros.stf.test.followmode.FollowModeDisabledInNewSessionTest;
import saros.stf.test.followmode.FollowModeTest;
import saros.stf.test.followmode.RefactorInFollowModeTest;
import saros.stf.test.followmode.SimpleFollowModeIITest;
import saros.stf.test.followmode.SimpleFollowModeITest;
import saros.stf.test.html.AddContactTest;
import saros.stf.test.html.StartSessionWizardTest;
import saros.stf.test.invitation.HostInvitesBelatedlyTest;
import saros.stf.test.invitation.InviteWithDifferentVersionsTest;
import saros.stf.test.invitation.Share2UsersSequentiallyInstantSessionTest;
import saros.stf.test.invitation.Share2UsersSequentiallyTest;
import saros.stf.test.invitation.ShareProjectUsingExistingProjectTest;
import saros.stf.test.partialsharing.ModifyNonSharedFilesTest;
import saros.stf.test.partialsharing.ShareFilesFromOneProjectToMultipleRemoteProjectsTest;
import saros.stf.test.partialsharing.ShareFilesToProjectsWithDifferentEncodingTest;
import saros.stf.test.permissions.WriteAccessChangeAndImmediateWriteTest;
import saros.stf.test.roster.HandleContactsTest;
import saros.stf.test.session.DerivedResourcesTest;
import saros.stf.test.session.EditFileThatIsNotOpenOnRemoteSideTest;
import saros.stf.test.session.EstablishSessionWithDifferentTransportModesTest;
import saros.stf.test.session.OverlappingSharingTest;
import saros.stf.test.whiteboard.ObjectCreationTest;

//order of the test classes is important

@RunWith(Suite.class)
@Suite.SuiteClasses({ Share2UsersSequentiallyTest.class,
    ChatViewFunctionsTest.class, AddMultipleFilesTest.class,
    ModifyFileWithoutEditorTest.class, RecoveryWhileTypingTest.class,
    ConcurrentEditingTest.class, EditDifferentFilesTest.class,
    Editing3ProjectsTest.class, EditWithReadAccessOnlyTest.class,
    FolderOperationsTest.class, SimpleFollowModeIITest.class,
    FollowModeDisabledInNewSessionTest.class, FollowModeTest.class,
    RefactorInFollowModeTest.class, SimpleFollowModeITest.class,
    ObjectCreationTest.class, WriteAccessChangeAndImmediateWriteTest.class,
    HostInvitesBelatedlyTest.class, InviteWithDifferentVersionsTest.class,
    ShareProjectUsingExistingProjectTest.class, ModifyNonSharedFilesTest.class,
    ShareFilesFromOneProjectToMultipleRemoteProjectsTest.class,
    ShareFilesToProjectsWithDifferentEncodingTest.class,
    HandleContactsTest.class, EditFileThatIsNotOpenOnRemoteSideTest.class,
    Share2UsersSequentiallyInstantSessionTest.class,
    EstablishSessionWithDifferentTransportModesTest.class, AddContactTest.class,
    StartSessionWizardTest.class, DerivedResourcesTest.class,
    OverlappingSharingTest.class })
public class AliceAndBobTestSuite {
    //
}
