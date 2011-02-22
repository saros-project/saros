package de.fu_berlin.inf.dpp.activities.business;

/**
 * A Receiver is an interface for handling multiple dispatch in Java.
 * 
 * In our case we want to call one of the specialized receive methods in
 * IActivityReceiver for a given IActivity.
 * 
 * For instance, if an IActivity is a TextSelectionActivity, we want the method
 * {@link #receive(TextSelectionActivity)} to be called.
 * 
 */
public interface IActivityReceiver {

    void receive(ViewportActivity viewportActivity);

    void receive(TextSelectionActivity textSelectionActivity);

    void receive(TextEditActivity textEditActivity);

    void receive(PermissionActivity permissionActivity);

    void receive(FolderActivity folderActivity);

    void receive(FileActivity fileActivity);

    void receive(EditorActivity editorActivity);

    void receive(JupiterActivity jupiterActivity);

    void receive(StopActivity stopActivity);

    void receive(ChecksumActivity checksumActivity);

    void receive(PingPongActivity pingPongActivity);

    void receive(ChecksumErrorActivity checksumErrorActivity);

    void receive(ProgressActivity progressActivity);

    void receive(VCSActivity activity);

    void receive(ChangeColorActivity changeColorActivity);

    void receive(ProjectsAddedActivity fileListActivity);
}
