package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import saros.stf.test.consistency.CreateSameFileAtOnceTest;
import saros.stf.test.consistency.EditDuringInvitationStressTest;
import saros.stf.test.consistency.EditDuringInvitationTest;
import saros.stf.test.consistency.EditDuringNonHostAddsProjectTest;
import saros.stf.test.consistency.EditDuringNonHostInvitationTest;
import saros.stf.test.consistency.ModifyDocumentBeforeProjectNegotiationTest;
import saros.stf.test.editing.ConcurrentEditingInsert100CharactersTest;
import saros.stf.test.editing.ConcurrentEditingWith3UsersTest;
import saros.stf.test.filefolderoperations.FileOperationsTest;
import saros.stf.test.invitation.AwarenessInformationVisibleAfterInvitationTest;
import saros.stf.test.invitation.InviteAndLeaveStressInstantSessionTest;
import saros.stf.test.invitation.InviteAndLeaveStressTest;
import saros.stf.test.invitation.NonHostInvitesContactTest;
import saros.stf.test.invitation.Share3UsersConcurrentlyTest;
import saros.stf.test.invitation.Share3UsersLeavingSessionTest;
import saros.stf.test.invitation.Share3UsersSequentiallyTest;
import saros.stf.test.invitation.UserDeclinesInvitationToCurrentSessionTest;
import saros.stf.test.roster.SortContactsOnlineOverOfflineTest;
import saros.stf.test.session.CreatingNewFileTest;
import saros.stf.test.session.ShareMultipleProjectsTest;
//order of the test classes is important

@RunWith(Suite.class)
@Suite.SuiteClasses({
    // independent tests
    Share3UsersConcurrentlyTest.class, EditDuringInvitationTest.class,
    EditDuringInvitationStressTest.class, Share3UsersSequentiallyTest.class,
    ModifyDocumentBeforeProjectNegotiationTest.class,
    AwarenessInformationVisibleAfterInvitationTest.class,
    InviteAndLeaveStressTest.class,
    InviteAndLeaveStressInstantSessionTest.class,
    NonHostInvitesContactTest.class, SortContactsOnlineOverOfflineTest.class,
    ShareMultipleProjectsTest.class, FileOperationsTest.class,
    // tests that need a session for ALICE&BOB&CARL
    CreateSameFileAtOnceTest.class, // selectFirst
    ConcurrentEditingInsert100CharactersTest.class,
    ConcurrentEditingWith3UsersTest.class,
    EditDuringNonHostAddsProjectTest.class, CreatingNewFileTest.class, // tearDownLast
    // tests that need a session for ALICE&BOB
    Share3UsersLeavingSessionTest.class, // selectFirst
    UserDeclinesInvitationToCurrentSessionTest.class,
    EditDuringNonHostInvitationTest.class // tearDownLast

})
public class AliceAndBobAndCarlTestSuite {
    //
}
