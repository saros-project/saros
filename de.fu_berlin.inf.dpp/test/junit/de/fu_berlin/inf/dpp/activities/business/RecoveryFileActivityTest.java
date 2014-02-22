package de.fu_berlin.inf.dpp.activities.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Type;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public class RecoveryFileActivityTest extends FileActivityTest {

    protected IFile file;
    protected SPath newPath;

    @Override
    @Before
    public void setup() {
        setupDefaultMocks();

        newPath = SarosMocks.mockSPath();
        paths = toListPlusNull(newPath);
        replayDefaultMocks();
    }

    /**
     * Test for the method RecoveryFileActivity.created()
     */
    @Test
    public void testCreated() {
        IActivity activity = null;

        activity = RecoveryFileActivity.created(source, newPath, data, target);

        if (activity == null) {
            fail("No Activity has been returned");
        } else {

            assertTrue(activity instanceof RecoveryFileActivity);
            RecoveryFileActivity recovery = (RecoveryFileActivity) activity;

            checkCorrectnessOfCommonParameters(recovery, Type.CREATED);

            assertTrue(ArrayUtils.isEquals(recovery.getContents(), data));
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
                Purpose.RECOVERY), target);

        if (activity == null) {
            fail("No Activity was created");
        } else {
            assertTrue(activity instanceof RecoveryFileActivity);

            RecoveryFileActivity recovery = (RecoveryFileActivity) activity;

            checkCorrectnessOfCommonParameters(recovery, Type.CREATED);

            assertTrue(ArrayUtils.isEquals(recovery.getContents(), data));
        }
    }

    /**
     * Tests that Activities created from FileActivities with a wrong Purpose
     * throw an exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromFileActivityWrongPurpose() {
        RecoveryFileActivity.createFromFileActivity(new FileActivity(source,
            Type.CREATED, newPath, null, data, Purpose.ACTIVITY), target);
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

        for (User target : targets) {
            for (Type type : types) {
                for (SPath newPath : paths) {
                    for (SPath oldPath : oldPaths) {
                        for (byte[] data : datas) {
                            RecoveryFileActivity rfa;
                            try {
                                rfa = new RecoveryFileActivity(source, target,
                                    type, newPath, oldPath, data);
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
