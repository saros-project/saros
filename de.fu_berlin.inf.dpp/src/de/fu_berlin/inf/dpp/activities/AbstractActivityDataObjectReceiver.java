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
 * Abstract implementation of IActivityDataObjectReceiver which does nothing
 */
public class AbstractActivityDataObjectReceiver implements IActivityDataObjectReceiver {

    public void receive(ViewportActivityDataObject viewportActivityDataObject) {
        // do nothing
    }

    public void receive(TextSelectionActivityDataObject textSelectionActivityDataObject) {
        // do nothing
    }

    public void receive(TextEditActivityDataObject textEditActivityDataObject) {
        // do nothing
    }

    public void receive(RoleActivityDataObject roleActivityDataObject) {
        // do nothing
    }

    public void receive(FolderActivityDataObject folderActivityDataObject) {
        // do nothing
    }

    public void receive(FileActivityDataObject fileActivityDataObject) {
        // do nothing
    }

    public void receive(EditorActivityDataObject editorActivityDataObject) {
        // do nothing
    }

    public void receive(JupiterActivity jupiterActivity) {
        // do nothing
    }

    public void receive(StopActivityDataObject stopActivityDataObject) {
        // do nothing
    }

    public void receive(PingPongActivityDataObject pingPongActivityDataObject) {
        // do nothing
    }

    public void receive(ChecksumActivityDataObject checksumActivityDataObject) {
        // do nothing
    }

    /**
     * Returns a IActivityDataObjectConsumer from the given IActivityDataObjectReceiver which
     * returns the given value for all calls to consume after calling receive on
     * the IActivityDataObjectReceiver.
     */
    public static IActivityDataObjectConsumer asConsumer(
        final IActivityDataObjectReceiver receiver, final boolean consume) {

        return new IActivityDataObjectConsumer() {

            public boolean consume(ViewportActivityDataObject viewportActivityDataObject) {
                receiver.receive(viewportActivityDataObject);
                return consume;
            }

            public boolean consume(TextSelectionActivityDataObject textSelectionActivityDataObject) {
                receiver.receive(textSelectionActivityDataObject);
                return consume;
            }

            public boolean consume(TextEditActivityDataObject textEditActivityDataObject) {
                receiver.receive(textEditActivityDataObject);
                return consume;
            }

            public boolean consume(RoleActivityDataObject roleActivityDataObject) {
                receiver.receive(roleActivityDataObject);
                return consume;
            }

            public boolean consume(FolderActivityDataObject folderActivityDataObject) {
                receiver.receive(folderActivityDataObject);
                return consume;
            }

            public boolean consume(FileActivityDataObject fileActivityDataObject) {
                receiver.receive(fileActivityDataObject);
                return consume;
            }

            public boolean consume(EditorActivityDataObject editorActivityDataObject) {
                receiver.receive(editorActivityDataObject);
                return consume;
            }

            public boolean consume(JupiterActivity jupiterActivity) {
                receiver.receive(jupiterActivity);
                return consume;
            }

            public boolean consume(StopActivityDataObject stopActivityDataObject) {
                receiver.receive(stopActivityDataObject);
                return consume;
            }

            public boolean consume(PingPongActivityDataObject pingPongActivityDataObject) {
                receiver.receive(pingPongActivityDataObject);
                return consume;
            }

            public boolean consume(ChecksumActivityDataObject checksumActivityDataObject) {
                receiver.receive(checksumActivityDataObject);
                return consume;
            }
        };
    }

}
