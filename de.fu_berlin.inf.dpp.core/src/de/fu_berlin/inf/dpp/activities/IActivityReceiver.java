package de.fu_berlin.inf.dpp.activities;

/**
 * This interface is for implementing multiple dispatch in Java (see <a
 * href="http://en.wikipedia.org/wiki/Multiple_dispatch">Wikipedia entry</a>).
 * <p>
 * In our case we want to call one of the specialized {@code receive()} methods
 * in {@link IActivityReceiver} for a given {@link IActivity}. For instance, if
 * an activity is a {@link TextSelectionActivity}, we want the method
 * {@link #receive(TextSelectionActivity)} to be called.
 */
public interface IActivityReceiver {
    /**
     * @JTourBusStop 3, Creating a new Activity type, Triple dispatch interface:
     * 
     *               This interface provides a receive() method for each
     *               activity implementation. This is part of the so-called
     *               "triple dispatch" (see "Activity sending" tour). For now,
     *               it is enough to know that you need to declare a new
     *               receive() method with your new Activity type in this
     *               interface (follow the alphabet, please), and then go to the
     *               next stop in this tour.
     */

    /***/

    void receive(ChangeColorActivity changeColorActivity);

    void receive(ChecksumActivity checksumActivity);

    void receive(ChecksumErrorActivity checksumErrorActivity);

    void receive(EditorActivity editorActivity);

    void receive(FileActivity fileActivity);

    void receive(FolderActivity folderActivity);

    /**
     * This method is called, when an {@link IDEInteractionActivity} was
     * received.
     * 
     * @param ideInteractionActivity
     *            The received {@link IDEInteractionActivity}
     * */
    void receive(IDEInteractionActivity ideInteractionActivity);

    void receive(JupiterActivity jupiterActivity);

    void receive(NOPActivity nopActivity);

    void receive(PermissionActivity permissionActivity);

    void receive(ProgressActivity progressActivity);

    /**
     * This method is called, when an {@link RefactoringActivity} was received.
     * 
     * @param refactoringActivity
     *            The received {@link RefactoringActivity}
     * */
    void receive(RefactoringActivity refactoringActivity);

    void receive(ShareConsoleActivity shareConsoleActivity);

    void receive(StartFollowingActivity startFollowingActivity);

    void receive(StopActivity stopActivity);

    void receive(StopFollowingActivity stopFollowingActivity);

    /**
     * This method is called, when a {@link TestRunActivity} was received.
     * 
     * @param testRunActivity
     *            The received {@link TestRunActivity}
     * */
    void receive(TestRunActivity testRunActivity);

    void receive(TextEditActivity textEditActivity);

    void receive(TextSelectionActivity textSelectionActivity);

    void receive(ViewportActivity viewportActivity);

    void receive(VCSActivity activity);

}
