package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.PingPongActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.RoleActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.StopActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

/**
 * An IActivityDataObjectConsumer is an interface for handling multiple dispatch in Java
 * (see IActivityDataObjectReceiver) but in addition provides a return value for
 * indicating that the activityDataObject was consumed (return true) or not (return false)
 * 
 * The typical use case is to extend {@link AbstractActivityDataObjectConsumer} and given
 * an {@link IActivityDataObject}, call {@link IActivityDataObject#dispatch(IActivityDataObjectConsumer)}
 * which will call the consume method matching the actual type of the IActivityDataObject.
 */
public interface IActivityDataObjectConsumer {

    boolean consume(ViewportActivityDataObject viewportActivityDataObject);

    boolean consume(TextSelectionActivityDataObject textSelectionActivityDataObject);

    boolean consume(TextEditActivityDataObject textEditActivityDataObject);

    boolean consume(RoleActivityDataObject roleActivityDataObject);

    boolean consume(FolderActivityDataObject folderActivityDataObject);

    boolean consume(FileActivityDataObject fileActivityDataObject);

    boolean consume(EditorActivityDataObject editorActivityDataObject);

    boolean consume(JupiterActivity jupiterActivity);

    boolean consume(StopActivityDataObject stopActivityDataObject);

    boolean consume(ChecksumActivityDataObject checksumActivityDataObject);

    boolean consume(PingPongActivityDataObject pingPongActivityDataObject);
}
