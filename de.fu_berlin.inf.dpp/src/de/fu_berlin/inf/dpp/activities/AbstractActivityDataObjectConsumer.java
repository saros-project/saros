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
 * Abstract implementation of an {@link IActivityDataObjectConsumer} which does nothing
 * and returns false for all consume methods.
 * 
 * This class is meant to be sub-classed to implement actual behavior.
 */
public class AbstractActivityDataObjectConsumer implements IActivityDataObjectConsumer {

    public boolean consume(ViewportActivityDataObject viewportActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(TextSelectionActivityDataObject textSelectionActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(TextEditActivityDataObject textEditActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(RoleActivityDataObject roleActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(FolderActivityDataObject folderActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(FileActivityDataObject fileActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(EditorActivityDataObject editorActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(JupiterActivity jupiterActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(StopActivityDataObject stopActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(PingPongActivityDataObject pingPongActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(ChecksumActivityDataObject checksumActivityDataObject) {
        // empty implementation
        return false;
    }

    /**
     * Utility method for converting a IActivityDataObjectConsumer into an
     * IActivityDataObjectReceiver (the result from the calls to consume are ignored)
     */
    public static IActivityDataObjectReceiver asReceiver(final IActivityDataObjectConsumer consumer) {
        return new IActivityDataObjectReceiver() {

            public void receive(ViewportActivityDataObject viewportActivityDataObject) {
                consumer.consume(viewportActivityDataObject);
            }

            public void receive(TextSelectionActivityDataObject textSelectionActivityDataObject) {
                consumer.consume(textSelectionActivityDataObject);
            }

            public void receive(TextEditActivityDataObject textEditActivityDataObject) {
                consumer.consume(textEditActivityDataObject);
            }

            public void receive(RoleActivityDataObject roleActivityDataObject) {
                consumer.consume(roleActivityDataObject);
            }

            public void receive(FolderActivityDataObject folderActivityDataObject) {
                consumer.consume(folderActivityDataObject);
            }

            public void receive(FileActivityDataObject fileActivityDataObject) {
                consumer.consume(fileActivityDataObject);
            }

            public void receive(EditorActivityDataObject editorActivityDataObject) {
                consumer.consume(editorActivityDataObject);
            }

            public void receive(JupiterActivity jupiterActivity) {
                consumer.consume(jupiterActivity);
            }

            public void receive(StopActivityDataObject stopActivityDataObject) {
                consumer.consume(stopActivityDataObject);
            }

            public void receive(PingPongActivityDataObject pingPongActivityDataObject) {
                consumer.consume(pingPongActivityDataObject);
            }

            public void receive(ChecksumActivityDataObject checksumActivityDataObject) {
                consumer.consume(checksumActivityDataObject);
            }
        };
    }

}
