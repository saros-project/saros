package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

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

    void receive(RoleActivity roleActivity);

    void receive(FolderActivity folderActivity);

    void receive(FileActivity fileActivity);

    void receive(EditorActivity editorActivity);

    void receive(JupiterActivity jupiterActivity);

    void receive(StopActivity stopActivity);

    void receive(ChecksumActivity checksumActivity);

    void receive(PingPongActivity pingPongActivity);
}
