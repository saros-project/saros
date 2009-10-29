package de.fu_berlin.inf.dpp.activities.business;

/**
 * Abstract implementation of IActivityReceiver which does nothing
 */
public class AbstractActivityReceiver implements IActivityReceiver {

    public void receive(ViewportActivity viewportActivityDataObject) {
        // do nothing
    }

    public void receive(TextSelectionActivity textSelectionActivityDataObject) {
        // do nothing
    }

    public void receive(TextEditActivity textEditActivityDataObject) {
        // do nothing
    }

    public void receive(RoleActivity roleActivityDataObject) {
        // do nothing
    }

    public void receive(FolderActivity folderActivityDataObject) {
        // do nothing
    }

    public void receive(FileActivity fileActivityDataObject) {
        // do nothing
    }

    public void receive(EditorActivity editorActivityDataObject) {
        // do nothing
    }

    public void receive(JupiterActivity jupiterActivity) {
        // do nothing
    }

    public void receive(StopActivity stopActivityDataObject) {
        // do nothing
    }

    public void receive(PingPongActivity pingPongActivityDataObject) {
        // do nothing
    }

    public void receive(ChecksumActivity checksumActivityDataObject) {
        // do nothing
    }

    /**
     * Returns a IActivityConsumer from the given IActivityReceiver which
     * returns the given value for all calls to consume after calling receive on
     * the IActivityReceiver.
     */
    public static IActivityConsumer asConsumer(
        final IActivityReceiver receiver, final boolean consume) {

        return new IActivityConsumer() {

            public boolean consume(ViewportActivity viewportActivityDataObject) {
                receiver.receive(viewportActivityDataObject);
                return consume;
            }

            public boolean consume(
                TextSelectionActivity textSelectionActivityDataObject) {
                receiver.receive(textSelectionActivityDataObject);
                return consume;
            }

            public boolean consume(TextEditActivity textEditActivityDataObject) {
                receiver.receive(textEditActivityDataObject);
                return consume;
            }

            public boolean consume(RoleActivity roleActivityDataObject) {
                receiver.receive(roleActivityDataObject);
                return consume;
            }

            public boolean consume(FolderActivity folderActivityDataObject) {
                receiver.receive(folderActivityDataObject);
                return consume;
            }

            public boolean consume(FileActivity fileActivityDataObject) {
                receiver.receive(fileActivityDataObject);
                return consume;
            }

            public boolean consume(EditorActivity editorActivityDataObject) {
                receiver.receive(editorActivityDataObject);
                return consume;
            }

            public boolean consume(JupiterActivity jupiterActivity) {
                receiver.receive(jupiterActivity);
                return consume;
            }

            public boolean consume(StopActivity stopActivityDataObject) {
                receiver.receive(stopActivityDataObject);
                return consume;
            }

            public boolean consume(PingPongActivity pingPongActivityDataObject) {
                receiver.receive(pingPongActivityDataObject);
                return consume;
            }

            public boolean consume(ChecksumActivity checksumActivityDataObject) {
                receiver.receive(checksumActivityDataObject);
                return consume;
            }
        };
    }

}
