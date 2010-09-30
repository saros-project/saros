package de.fu_berlin.inf.dpp.activities.business;

/**
 * Abstract implementation of IActivityReceiver which does nothing.
 * 
 * Useful, if just interested in some particular IActivity
 */
public class AbstractActivityReceiver implements IActivityReceiver {

    public void receive(ViewportActivity viewportActivity) {
        // do nothing
    }

    public void receive(TextSelectionActivity textSelectionActivity) {
        // do nothing
    }

    public void receive(TextEditActivity textEditActivity) {
        // do nothing
    }

    public void receive(RoleActivity roleActivity) {
        // do nothing
    }

    public void receive(FolderActivity folderActivity) {
        // do nothing
    }

    public void receive(FileActivity fileActivity) {
        // do nothing
    }

    public void receive(EditorActivity editorActivity) {
        // do nothing
    }

    public void receive(JupiterActivity jupiterActivity) {
        // do nothing
    }

    public void receive(StopActivity stopActivity) {
        // do nothing
    }

    public void receive(PingPongActivity pingPongActivity) {
        // do nothing
    }

    public void receive(ChecksumActivity checksumActivity) {
        // do nothing
    }

    public void receive(ChecksumErrorActivity checksumErrorActivity) {
        // do nothing
    }

    public void receive(ProgressActivity progressActivity) {
        // do nothing
    }

    public void receive(VCSActivity activity) {
        // do nothing
    }

    public void receive(ChangeColorActivity changeColorActivity) {
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

            public boolean consume(ViewportActivity viewportActivity) {
                receiver.receive(viewportActivity);
                return consume;
            }

            public boolean consume(TextSelectionActivity textSelectionActivity) {
                receiver.receive(textSelectionActivity);
                return consume;
            }

            public boolean consume(TextEditActivity textEditActivity) {
                receiver.receive(textEditActivity);
                return consume;
            }

            public boolean consume(RoleActivity roleActivity) {
                receiver.receive(roleActivity);
                return consume;
            }

            public boolean consume(FolderActivity folderActivity) {
                receiver.receive(folderActivity);
                return consume;
            }

            public boolean consume(FileActivity fileActivity) {
                receiver.receive(fileActivity);
                return consume;
            }

            public boolean consume(EditorActivity editorActivity) {
                receiver.receive(editorActivity);
                return consume;
            }

            public boolean consume(JupiterActivity jupiterActivity) {
                receiver.receive(jupiterActivity);
                return consume;
            }

            public boolean consume(StopActivity stopActivity) {
                receiver.receive(stopActivity);
                return consume;
            }

            public boolean consume(PingPongActivity pingPongActivity) {
                receiver.receive(pingPongActivity);
                return consume;
            }

            public boolean consume(ChecksumActivity checksumActivity) {
                receiver.receive(checksumActivity);
                return consume;
            }

            public boolean consume(ChecksumErrorActivity checksumErrorActivity) {
                receiver.receive(checksumErrorActivity);
                return consume;
            }

            public boolean consume(ProgressActivity progressActivity) {
                receiver.receive(progressActivity);
                return consume;
            }

            public boolean consume(VCSActivity activity) {
                receiver.receive(activity);
                return consume;
            }

            public boolean consume(ChangeColorActivity changeColorActivity) {
                receiver.receive(changeColorActivity);
                return consume;
            }
        };
    }
}
