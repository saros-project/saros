package de.fu_berlin.inf.dpp.activities.business;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.serializable.RecoveryFileActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.FileUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class RecoveryFileActivityTest {

    private static final byte[] data = new byte[] { 'a', 'b', 'c' };
    private long checksum = 12345L;
    private User source;
    private User target;
    private SPath newPath;
    private IFile file;
    private ISarosSession session;
    private SPathDataObject newPathDataObject;

    @Before
    public void setUp() throws CoreException, IOException {

        // FileMock is needed for SPathMock
        file = EasyMock.createMock(IFile.class);
        EasyMock.expect(file.getContents()).andStubReturn(
            new ByteArrayInputStream(data));
        EasyMock.expect(file.exists()).andStubReturn(Boolean.TRUE);
        EasyMock.replay(file);

        // Mocks for User
        source = EasyMock.createMock(User.class);
        EasyMock.expect(source.getJID()).andStubReturn(
            new JID("alice@jabber.org"));
        EasyMock.replay(source);

        target = EasyMock.createMock(User.class);
        EasyMock.expect(target.getJID()).andStubReturn(
            new JID("bob@jabber.org"));
        EasyMock.replay(target);

        // SessionMock
        session = EasyMock.createMock(ISarosSession.class);
        EasyMock.expect(session.getUser(source.getJID())).andReturn(source);
        EasyMock.expect(session.getUser(target.getJID())).andReturn(target);
        EasyMock.replay(session);

        // create Mocks for SPath and SPathDataObject for transforming them into
        // each other
        newPath = EasyMock.createMock(SPath.class);
        newPathDataObject = EasyMock.createMock(SPathDataObject.class);

        EasyMock.expect(newPath.getFile()).andStubReturn(file);
        EasyMock.expect(newPath.toSPathDataObject(session)).andReturn(
            newPathDataObject);
        EasyMock.expect(newPathDataObject.toSPath(session)).andReturn(newPath);

        EasyMock.replay(newPathDataObject, newPath);

        // Mock calls to FileUtils done by the superclass
        mockStaticPartial(FileUtils.class, "checksum");

        FileUtils.checksum(isA(IFile.class));
        expectLastCall().andStubReturn(checksum);

        replayAll();

    }

    /**
     * Test for the method RecoveryFileActivity.created()
     */
    @Test
    public void testCreated() {
        IActivity activity = null;
        try {
            activity = RecoveryFileActivity.created(source, newPath, target);
        } catch (IOException e) {
            fail("Creation of Actvity failed");
        }
        if (activity == null) {
            fail("No Activity has been returned");
        } else {

            assertTrue(activity instanceof RecoveryFileActivity);
            RecoveryFileActivity recovery = (RecoveryFileActivity) activity;

            checkCorrectnessOfCommonParameters(recovery, Type.CREATED);

            assertTrue(ArrayUtils.isEquals(recovery.getContents(), data));
            assertEquals(checksum, recovery.getChecksum());
        }

    }

    /**
     * Test for the method RecoveryFileActivity.removed()
     */
    @Test
    public void testRemoved() {

        IActivity activity = RecoveryFileActivity.removed(source, newPath,
            target);
        if (activity == null) {
            fail("No Activity has been returned");
        } else {
            assertTrue(activity instanceof RecoveryFileActivity);

            RecoveryFileActivity recovery = (RecoveryFileActivity) activity;

            checkCorrectnessOfCommonParameters(recovery, Type.REMOVED);
        }

    }

    /**
     * Test for the method RecoveryFileActivity.createFromFileActivity
     */
    @Test
    public void testCreateFromFileActivity() {

        IActivity activity = RecoveryFileActivity.createFromFileActivity(
            new FileActivity(source, Type.CREATED, newPath, null, data,
                Purpose.RECOVERY, checksum), target);
        if (activity == null) {
            fail("No Activity was created");
        } else {
            assertTrue(activity instanceof RecoveryFileActivity);

            RecoveryFileActivity recovery = (RecoveryFileActivity) activity;

            checkCorrectnessOfCommonParameters(recovery, Type.CREATED);

            assertTrue(ArrayUtils.isEquals(recovery.getContents(), data));
            assertEquals(checksum, recovery.getChecksum());
        }

    }

    /**
     * Tests that Activities created from FileActivities with a wrong Purpose
     * throw an exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromFileActivityWrongPurpose() {

        IActivity activity = RecoveryFileActivity.createFromFileActivity(
            new FileActivity(source, Type.CREATED, newPath, null, data,
                Purpose.ACTIVITY, checksum), target);
    }

    /**
     * Tests if the conversion into ADO and back keeps the content of the
     * Activity intact
     */
    @Test
    public void testConversion() {
        RecoveryFileActivity activity = new RecoveryFileActivity(source,
            target, Type.CREATED, newPath, null, data, checksum);
        RecoveryFileActivityDataObject ado = (RecoveryFileActivityDataObject) activity
            .getActivityDataObject(session);
        RecoveryFileActivity transformed = (RecoveryFileActivity) ado
            .getActivity(session);
        assertTrue("Transformed Activity is different.",
            activity.equals(transformed));
        // sanitycheck for Equals
        transformed = new RecoveryFileActivity(target, target, Type.CREATED,
            newPath, null, data, 12l);
        assertFalse("Equals couldn't detect differences",
            activity.equals(transformed));
    }

    private void checkCorrectnessOfCommonParameters(
        RecoveryFileActivity activity, Type type) {

        assertEquals(source, activity.getSource());
        assertEquals(target, activity.getRecipients().get(0));
        assertEquals(type, activity.getType());
        assertEquals(newPath, activity.getPath());
        assertNull(activity.getOldPath());
        assertTrue(activity.isRecovery());
    }
}
