package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

/**
 * An IActivityConsumer is an interface for handling multiple dispatch in Java
 * (see IActivityReceiver) but in addition provides a return value for
 * indicating that the activity was consumed (return true) or not (return false)
 * 
 * The typical use case is to extend {@link AbstractActivityConsumer} and given
 * an {@link IActivity}, call {@link IActivity#dispatch(IActivityConsumer)}
 * which will call the consume method matching the actual type of the IActivity.
 */
public interface IActivityConsumer {

    boolean consume(ViewportActivity viewportActivity);

    boolean consume(TextSelectionActivity textSelectionActivity);

    boolean consume(TextEditActivity textEditActivity);

    boolean consume(RoleActivity roleActivity);

    boolean consume(FolderActivity folderActivity);

    boolean consume(FileActivity fileActivity);

    boolean consume(EditorActivity editorActivity);

    boolean consume(JupiterActivity jupiterActivity);

    boolean consume(StopActivity stopActivity);

    boolean consume(PingPongActivity pingPongActivity);
}
