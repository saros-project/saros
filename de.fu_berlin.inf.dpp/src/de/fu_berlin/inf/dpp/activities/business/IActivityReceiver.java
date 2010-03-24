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

    void receive(ViewportActivity viewportActivityDataObject);

    void receive(TextSelectionActivity textSelectionActivityDataObject);

    void receive(TextEditActivity textEditActivityDataObject);

    void receive(RoleActivity roleActivityDataObject);

    void receive(FolderActivity folderActivityDataObject);

    void receive(FileActivity fileActivityDataObject);

    void receive(EditorActivity editorActivityDataObject);

    void receive(JupiterActivity jupiterActivity);

    void receive(StopActivity stopActivityDataObject);

    void receive(ChecksumActivity checksumActivityDataObject);

    void receive(PingPongActivity pingPongActivityDataObject);
}
