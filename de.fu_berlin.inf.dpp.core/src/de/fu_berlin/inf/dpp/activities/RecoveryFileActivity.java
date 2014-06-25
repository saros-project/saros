package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
@XStreamAlias("recoveryFileActivity")
public class RecoveryFileActivity extends FileActivity implements
    ITargetedActivity {

    @XStreamAsAttribute
    private User target;

    public RecoveryFileActivity(User source, User target, Type type,
        SPath newPath, SPath oldPath, byte[] data, String encoding) {

        super(source, type, newPath, oldPath, data, encoding, Purpose.RECOVERY);

        if (target == null)
            throw new IllegalArgumentException("target must not be null");

        this.target = target;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (target != null);
    }

    @Override
    public User getTarget() {
        return target;
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
        byte[] content, User target, String encoding) {

        FileActivity fileActivity = FileActivity.created(source, path, content,
            encoding, Purpose.RECOVERY);

        return createFromFileActivity(fileActivity, target, encoding);
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
     * @param encoding
     *            the encoding the content is encoded with or <code>null</code>
     */
    public static RecoveryFileActivity removed(User source, SPath path,
        User target, String encoding) {

        FileActivity fileActivity = FileActivity.removed(source, path,
            Purpose.RECOVERY);

        return createFromFileActivity(fileActivity, target, encoding);
    }

    /**
     * Creates a RecoveryFileActivity from a given FileActivity. The Purpose of
     * the FileActivity has to be {@link FileActivity.Purpose#RECOVERY}
     * 
     * @param activity
     *            The FileActivity to be transformed
     * @param target
     *            The User this Activity should be send to
     * @param encoding
     *            the encoding the content is encoded with or <code>null</code>
     */
    private static RecoveryFileActivity createFromFileActivity(
        FileActivity activity, User target, String encoding) {

        if (activity.purpose != Purpose.RECOVERY)
            throw new IllegalArgumentException();

        return new RecoveryFileActivity(activity.getSource(), target,
            activity.type, activity.getPath(), activity.oldPath,
            activity.content, encoding);
    }

    @Override
    public String toString() {
        return "RecoveryFileActivity [target=" + target + ", dst:path="
            + getPath() + ", src:path=" + (oldPath == null ? "N/A" : oldPath)
            + ", type=" + type + ", encoding="
            + (encoding == null ? "N/A" : encoding) + ", content="
            + (content == null ? "0" : content.length) + " byte(s)]";
    }
}
