package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;

/**
 * Abstract implementation of an {@link IActivityConsumer} which does nothing
 * and returns false for all consume methods.
 * 
 * This class is meant to be sub-classed to implement actual behavior.
 */
public class AbstractActivityConsumer implements IActivityConsumer {

    public boolean consume(ViewportActivity viewportActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(TextSelectionActivity textSelectionActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(TextEditActivity textEditActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(RoleActivity roleActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(FolderActivity folderActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(FileActivity fileActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(EditorActivity editorActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(JupiterActivity jupiterActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(StopActivity stopActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(PingPongActivity pingPongActivity) {
        // empty implementation
        return false;
    }

    /**
     * Utility method for converting a IActivityConsumer into an
     * IActivityReceiver (the result from the calls to consume are ignored)
     */
    public static IActivityReceiver asReceiver(final IActivityConsumer consumer) {
        return new IActivityReceiver() {

            public void receive(ViewportActivity viewportActivity) {
                consumer.consume(viewportActivity);
            }

            public void receive(TextSelectionActivity textSelectionActivity) {
                consumer.consume(textSelectionActivity);
            }

            public void receive(TextEditActivity textEditActivity) {
                consumer.consume(textEditActivity);
            }

            public void receive(RoleActivity roleActivity) {
                consumer.consume(roleActivity);
            }

            public void receive(FolderActivity folderActivity) {
                consumer.consume(folderActivity);
            }

            public void receive(FileActivity fileActivity) {
                consumer.consume(fileActivity);
            }

            public void receive(EditorActivity editorActivity) {
                consumer.consume(editorActivity);
            }

            public void receive(JupiterActivity jupiterActivity) {
                consumer.consume(jupiterActivity);
            }

            public void receive(StopActivity stopActivity) {
                consumer.consume(stopActivity);
            }

            public void receive(PingPongActivity pingPongActivity) {
                consumer.consume(pingPongActivity);
            }
        };
    }
}
