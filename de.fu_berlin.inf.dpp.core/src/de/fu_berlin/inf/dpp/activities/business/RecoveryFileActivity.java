package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.RecoveryFileActivityDataObject;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Subclass of FileActivity that is used during the Recovery-Process and allows
 * the specification of targets. This Activity will is sent from the host to the
 * client that requested the recovery.
 * 
 * TODO This class should be removed once the ITargetedActivities are gone since
 * it is only intended as a separation between FileActivities that are sent to a
 * singleUser and those send to every User
 */
public class RecoveryFileActivity extends FileActivity implements
    ITargetedActivity {

    private User target;

    public RecoveryFileActivity(User source, User target, Type type,
        SPath newPath, SPath oldPath, byte[] data) {

        super(source, type, newPath, oldPath, data, Purpose.RECOVERY);

        if (target == null)
            throw new IllegalArgumentException("target must not be null");

        this.target = target;
    }

    @Override
    public User getTarget() {
        return target;
    }

    @Override
    public IActivityDataObject getActivityDataObject(
        ISarosSession sarosSession, IPathFactory pathFactory) {
        return new RecoveryFileActivityDataObject(getSource().getJID(),
            target.getJID(), type, newPath, oldPath, data);
    }

    /**
     * Utility method for creating a RecoveryFileActivity of type
     * {@link FileActivity.Type#CREATED} for a given path.
     * 
     * This method will call the created()-method of the {@link FileActivity}.
     * This Method is used if the host has the file to be recovered.
     * 
     * @param source
     *            The User that has created this Activity.
     * @param path
     *            The SPath of the affected resource.
     * @param content
     *            content of the file denoted by the path
     * @param target
     *            The User this Activity will be send to.
     */
    public static RecoveryFileActivity created(User source, SPath path,
        byte[] content, User target) {

        FileActivity fileActivity = FileActivity.created(source, path, content,
            Purpose.RECOVERY);

        return createFromFileActivity(fileActivity, target);
    }

    /**
     * Utility method for creating a RecoveryFileActivity of type
     * {@link FileActivity.Type#REMOVED} for a given path.
     * 
     * This method will call the removed()-method of the {@link FileActivity}.
     * This Method is used if the file to be recovered doesn't exist on the
     * host.
     * 
     * @param source
     *            The User that has created this Activity.
     * @param path
     *            The SPath of the affected resource.
     * @param target
     *            The User this Activity will be send to.
     */
    public static RecoveryFileActivity removed(User source, SPath path,
        User target) {

        FileActivity fileActivity = FileActivity.removed(source, path,
            Purpose.RECOVERY);

        return createFromFileActivity(fileActivity, target);
    }

    /**
     * This method creates a RecoveryFileActivity from a given FileActivity. The
     * Purpose of the FileActivity has to be
     * {@link FileActivity.Purpose#RECOVERY}
     * 
     * @param activity
     *            The FileActivity to be transformed
     * @param target
     *            The User this Activity should be send to
     */
    public static RecoveryFileActivity createFromFileActivity(
        FileActivity activity, User target) {
        if (activity.purpose != Purpose.RECOVERY) {
            throw new IllegalArgumentException();
        }
        return new RecoveryFileActivity(activity.getSource(), target,
            activity.type, activity.newPath, activity.oldPath, activity.data);
    }
}
