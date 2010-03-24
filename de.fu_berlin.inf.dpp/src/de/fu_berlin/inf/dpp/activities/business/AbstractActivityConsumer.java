package de.fu_berlin.inf.dpp.activities.business;

/**
 * Abstract implementation of an {@link IActivityConsumer} which does nothing
 * and returns false for all consume methods.
 * 
 * This class is meant to be sub-classed to implement actual behavior.
 */
public class AbstractActivityConsumer implements IActivityConsumer {

    public boolean consume(ViewportActivity viewportActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(TextSelectionActivity textSelectionActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(TextEditActivity textEditActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(RoleActivity roleActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(FolderActivity folderActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(FileActivity fileActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(EditorActivity editorActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(JupiterActivity jupiterActivity) {
        // empty implementation
        return false;
    }

    public boolean consume(StopActivity stopActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(PingPongActivity pingPongActivityDataObject) {
        // empty implementation
        return false;
    }

    public boolean consume(ChecksumActivity checksumActivityDataObject) {
        // empty implementation
        return false;
    }

    /**
     * Utility method for converting a IActivityConsumer into an
     * IActivityReceiver (the result from the calls to consume are ignored)
     */
    public static IActivityReceiver asReceiver(final IActivityConsumer consumer) {
        return new IActivityReceiver() {

            public void receive(ViewportActivity viewportActivityDataObject) {
                consumer.consume(viewportActivityDataObject);
            }

            public void receive(
                TextSelectionActivity textSelectionActivityDataObject) {
                consumer.consume(textSelectionActivityDataObject);
            }

            public void receive(TextEditActivity textEditActivityDataObject) {
                consumer.consume(textEditActivityDataObject);
            }

            public void receive(RoleActivity roleActivityDataObject) {
                consumer.consume(roleActivityDataObject);
            }

            public void receive(FolderActivity folderActivityDataObject) {
                consumer.consume(folderActivityDataObject);
            }

            public void receive(FileActivity fileActivityDataObject) {
                consumer.consume(fileActivityDataObject);
            }

            public void receive(EditorActivity editorActivityDataObject) {
                consumer.consume(editorActivityDataObject);
            }

            public void receive(JupiterActivity jupiterActivity) {
                consumer.consume(jupiterActivity);
            }

            public void receive(StopActivity stopActivityDataObject) {
                consumer.consume(stopActivityDataObject);
            }

            public void receive(PingPongActivity pingPongActivityDataObject) {
                consumer.consume(pingPongActivityDataObject);
            }

            public void receive(ChecksumActivity checksumActivityDataObject) {
                consumer.consume(checksumActivityDataObject);
            }
        };
    }

}
