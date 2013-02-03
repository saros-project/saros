package de.fu_berlin.inf.dpp.activities.business;

/**
 * @JTourBusStop 6, Activity creation, Triple dispatch abstract class:
 * 
 *               Instead of having to create stubs for all IActivity implementations
 *               not handled you can extend this abstract class and just re-implement
 *               the method for your new IActivity implementation.
 */
/**
 * Abstract implementation of IActivityReceiver which does nothing.
 * 
 * Useful, if just interested in some particular IActivity
 */
public class AbstractActivityReceiver implements IActivityReceiver {

    @Override
    public void receive(ViewportActivity viewportActivity) {
        // do nothing
    }

    @Override
    public void receive(TextSelectionActivity textSelectionActivity) {
        // do nothing
    }

    @Override
    public void receive(TextEditActivity textEditActivity) {
        // do nothing
    }

    @Override
    public void receive(PermissionActivity permissionActivity) {
        // do nothing
    }

    @Override
    public void receive(FolderActivity folderActivity) {
        // do nothing
    }

    @Override
    public void receive(FileActivity fileActivity) {
        // do nothing
    }

    @Override
    public void receive(EditorActivity editorActivity) {
        // do nothing
    }

    @Override
    public void receive(JupiterActivity jupiterActivity) {
        // do nothing
    }

    @Override
    public void receive(StopActivity stopActivity) {
        // do nothing
    }

    @Override
    public void receive(PingPongActivity pingPongActivity) {
        // do nothing
    }

    @Override
    public void receive(ChecksumActivity checksumActivity) {
        // do nothing
    }

    @Override
    public void receive(ChecksumErrorActivity checksumErrorActivity) {
        // do nothing
    }

    @Override
    public void receive(ProgressActivity progressActivity) {
        // do nothing
    }

    @Override
    public void receive(VCSActivity activity) {
        // do nothing
    }

    @Override
    public void receive(ChangeColorActivity changeColorActivity) {
        // do nothing
    }

    @Override
    public void receive(ProjectsAddedActivity fileListActivity) {
        // do nothing

    }

    @Override
    public void receive(StartFollowingActivity startFollowingActivity) {
        // do nothing

    }

    @Override
    public void receive(StopFollowingActivity stopFollowingActivity) {
        // do nothing

    }

    @Override
    public void receive(NOPActivity nopActivity) {
        // do nothing

    }
}
