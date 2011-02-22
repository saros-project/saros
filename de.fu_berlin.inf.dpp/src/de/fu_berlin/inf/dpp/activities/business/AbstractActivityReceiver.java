package de.fu_berlin.inf.dpp.activities.business;

/**
 * Abstract implementation of IActivityReceiver which does nothing.
 * 
 * Useful, if just interested in some particular IActivity
 */
public class AbstractActivityReceiver implements IActivityReceiver {

    public void receive(ViewportActivity viewportActivity) {
        // do nothing
    }

    public void receive(TextSelectionActivity textSelectionActivity) {
        // do nothing
    }

    public void receive(TextEditActivity textEditActivity) {
        // do nothing
    }

    public void receive(PermissionActivity permissionActivity) {
        // do nothing
    }

    public void receive(FolderActivity folderActivity) {
        // do nothing
    }

    public void receive(FileActivity fileActivity) {
        // do nothing
    }

    public void receive(EditorActivity editorActivity) {
        // do nothing
    }

    public void receive(JupiterActivity jupiterActivity) {
        // do nothing
    }

    public void receive(StopActivity stopActivity) {
        // do nothing
    }

    public void receive(PingPongActivity pingPongActivity) {
        // do nothing
    }

    public void receive(ChecksumActivity checksumActivity) {
        // do nothing
    }

    public void receive(ChecksumErrorActivity checksumErrorActivity) {
        // do nothing
    }

    public void receive(ProgressActivity progressActivity) {
        // do nothing
    }

    public void receive(VCSActivity activity) {
        // do nothing
    }

    public void receive(ChangeColorActivity changeColorActivity) {
        // do nothing
    }

    public void receive(ProjectsAddedActivity fileListActivity) {
        // do nothing

    }
}
