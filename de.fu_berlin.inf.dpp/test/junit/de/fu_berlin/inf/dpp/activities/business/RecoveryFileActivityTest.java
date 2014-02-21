package de.fu_berlin.inf.dpp.activities.business;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;
import de.fu_berlin.inf.dpp.util.FileUtils;

public class RecoveryFileActivityTest extends FileActivityTest {

    protected IFile file;
    protected SPath newPath;

    @Override
    @Before
    public void setup() {
        setupDefaultMocks();

        file = SarosMocks.mockExistingIFile(data);
        expect(file.exists()).andStubReturn(Boolean.TRUE);
        expect(file.getType()).andStubReturn(IResource.FILE);
        expect(file.getAdapter(IFile.class)).andStubReturn(file);
        replay(file);

        newPath = SarosMocks.prepareMockSPath();

        expect(newPath.getFile()).andStubReturn(
            ResourceAdapterFactory.create(file));

        PowerMock.replay(newPath);

        paths = toListPlusNull(newPath);

        mockStaticPartial(FileUtils.class, "checksum");
        try {
            FileUtils.checksum(isA(IFile.class));
        } catch (IOException e) {
            // consumed, cannot happen
        }
        expectLastCall().andStubReturn(checksum);
        PowerMock.replay(FileUtils.class);

        replayDefaultMocks();
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
        RecoveryFileActivity.createFromFileActivity(new FileActivity(source,
            Type.CREATED, newPath, null, data, Purpose.ACTIVITY, checksum),
            target);
    }

    /**
     * Tests if the conversion into ADO and back keeps the content of the
     * Activity intact
     */
    @Override
    @Test
    public void testConversion() {
        // Mock remaining parameters
        List<Type> types = toListPlusNull(Type.values());
        List<SPath> oldPaths = toListPlusNull(SarosMocks.mockSPath());
        List<byte[]> datas = toListPlusNull(data);
        List<Long> checksums = toListPlusNull(new Long(1024L));

        for (User target : targets) {
            for (Type type : types) {
                for (SPath newPath : paths) {
                    for (SPath oldPath : oldPaths) {
                        for (byte[] data : datas) {
                            for (Long checksum : checksums) {
                                RecoveryFileActivity rfa;
                                try {
                                    rfa = new RecoveryFileActivity(source,
                                        target, type, newPath, oldPath, data,
                                        checksum);
                                } catch (IllegalArgumentException e) {
                                    continue;
                                }

                                testConversionAndBack(rfa);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkCorrectnessOfCommonParameters(
        RecoveryFileActivity activity, Type type) {

        assertEquals(source, activity.getSource());
        assertEquals(target, activity.getTarget());
        assertEquals(type, activity.getType());
        assertEquals(newPath, activity.getPath());
        assertNull(activity.getOldPath());
        assertTrue(activity.isRecovery());
    }
}
