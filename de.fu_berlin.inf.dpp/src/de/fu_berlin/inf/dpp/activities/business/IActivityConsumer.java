package de.fu_berlin.inf.dpp.activities.business;

/**
 * An IActivityConsumer is an interface for handling multiple dispatch in Java
 * (see IActivityReceiver) but in addition provides a return value for
 * indicating that the activityDataObject was consumed (return true) or not
 * (return false)
 * 
 * The typical use case is to extend {@link AbstractActivityConsumer} and given
 * an {@link IActivity}, call {@link IActivity#dispatch(IActivityConsumer)}
 * which will call the consume method matching the actual type of the IActivity.
 */
public interface IActivityConsumer {

    boolean consume(ViewportActivity viewportActivityDataObject);

    boolean consume(TextSelectionActivity textSelectionActivityDataObject);

    boolean consume(TextEditActivity textEditActivityDataObject);

    boolean consume(RoleActivity roleActivityDataObject);

    boolean consume(FolderActivity folderActivityDataObject);

    boolean consume(FileActivity fileActivityDataObject);

    boolean consume(EditorActivity editorActivityDataObject);

    boolean consume(JupiterActivity jupiterActivity);

    boolean consume(StopActivity stopActivityDataObject);

    boolean consume(ChecksumActivity checksumActivityDataObject);

    boolean consume(PingPongActivity pingPongActivityDataObject);

    boolean consume(ChecksumErrorActivity checksumErrorActivity);

    boolean consume(ProgressActivity progressActivity);

    boolean consume(VCSActivity vcsActivity);
}
