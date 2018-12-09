package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This collector collects information about selections made by users with {@link
 * Permission#READONLY_ACCESS} that are witnessed by the local user. The total selections are
 * counted and it is checked, if each selection was within the file the local user was viewing. He
 * might either see that selection directly within his viewport or through viewport annotation.
 *
 * <p>Selection made by users with {@link Permission#WRITE_ACCESS} are not analyzed as those will be
 * misleading.
 *
 * <p>Furthermore, if a user with {@link Permission#READONLY_ACCESS} selects some text and a change
 * to that specific text is done later on, this is stored as a (probable) successful gesture.
 */
@Component(module = "feedback")
public class SelectionCollector extends AbstractStatisticCollector {

  /** Key for total users with {@link Permission#READONLY_ACCESS} selection count */
  private static final String KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT =
      "observer.selection.count";

  /** Key for witnessed users with {@link Permission#READONLY_ACCESS} selections */
  private static final String KEY_WITNESSED_USER_WITH_READONLY_ACCESS_SELECTION_COUNT =
      "observer.selection.count.witnessed";

  /** Key for gesture count */
  private static final String KEY_GESTURE_COUNT = "gesture.count";

  /**
   * A SelectionEvent encapsulates information about a selection made. This information includes:
   *
   * <ul>
   *   <li>time of the event
   *   <li>{@link SPath} of the event
   *   <li>{@link User} who made the selection
   *   <li>offset of the selection
   *   <li>length of the selection
   *   <li><code>boolean</code> representing if selection was within scope of local user
   *   <li><code>boolean</code> flag if a text edit occurred within that selection
   * </ul>
   */
  private static class SelectionEvent {
    private long time;
    private SPath path;
    private int offset;
    private int length;
    private boolean withinFile;
    private boolean gestured;

    public SelectionEvent(
        long time, SPath path, int offset, int length, boolean withinFile, boolean gestured) {
      this.time = time;
      this.path = path;
      this.offset = offset;
      this.length = length;
      this.withinFile = withinFile;
      this.gestured = gestured;
    }
  }

  /** Represents the current active editor of the local {@link User} */
  private SPath localPath = null;

  /** A map where the latest selections information for each each user as {@link JID} are stored. */
  private final Map<User, SelectionEvent> activeSelections = new HashMap<User, SelectionEvent>();

  /**
   * A list where all selection events made by a remote user with {@link Permission#READONLY_ACCESS}
   * are being stored<br>
   */
  private final List<SelectionEvent> userWithReadOnlyAccessSelectionEvents =
      new ArrayList<SelectionEvent>();

  private final IEditorManager editorManager;

  private final ISharedEditorListener editorListener =
      new ISharedEditorListener() {

        @Override
        public void textEdited(TextEditActivity textEdit) {

          /*
           * for each edit check if it is made within the range of a current
           * active selection
           */
          for (Map.Entry<User, SelectionEvent> entry : activeSelections.entrySet()) {
            User currentUser = entry.getKey();
            SelectionEvent selection = entry.getValue();

            /*
             * only proceed if selection was not made by the editor himself
             * and the edit occurs within this selection (path and range).
             * If so, increment the gesture count and break. To prevent
             * multiple indicated gestures set the flag gestured to true.
             * This will prevent multiple possible gestures from just a
             * single selection - i.e. when the edit consists of more than
             * one character.
             */
            User user = textEdit.getSource();
            int offset = textEdit.getOffset();
            SPath filePath = textEdit.getPath();

            if (!currentUser.equals(user)
                && selection.offset <= offset
                && (selection.offset + selection.length) >= offset
                && (selection.path).equals(filePath)
                && !selection.gestured) {
              selection.gestured = true;
              break;
            }
          }
        }

        @Override
        public void editorActivated(User user, SPath filePath) {
          if (user.equals(sarosSession.getLocalUser()))
            // Remember which editor the local user has open.
            localPath = filePath;
        }

        /**
         * {@inheritDoc}
         *
         * <p>TODO Implement IActivityConsumer instead and delete this method from the
         * SharedEditorListener (this is the only implementation)
         */
        @Override
        public void textSelectionChanged(TextSelectionActivity selection) {
          // details of the occurred selection
          long time = System.currentTimeMillis();
          SPath path = selection.getPath();
          int offset = selection.getOffset();
          int length = selection.getLength();

          boolean withinFile = false;
          boolean gestured = false;

          User source = selection.getSource();

          /*
           * check if selection was made within the file the local user is
           * currently viewing
           */
          if (path.equals(localPath)) {
            withinFile = true;
          }

          SelectionEvent currentSelection =
              new SelectionEvent(time, path, offset, length, withinFile, gestured);

          /**
           * check if the selection was made by a user with {@link User.Permission#READONLY_ACCESS}
           * and has a length of more than 0
           */
          if (length > 0 && source.hasReadOnlyAccess()) {
            userWithReadOnlyAccessSelectionEvents.add(currentSelection);
            /*
             * check if there is already a selection stored for this user
             * and replace it in case or just store the selection if not
             */
            if (activeSelections.containsKey(source)) {
              activeSelections.remove(source);
              activeSelections.put(source, currentSelection);
            } else {
              activeSelections.put(source, currentSelection);
            }
          }
        }
      };

  public SelectionCollector(
      StatisticManager statisticManager, ISarosSession session, IEditorManager editorManager) {
    super(statisticManager, session);

    this.editorManager = editorManager;
  }

  @Override
  protected void processGatheredData() {
    // count for selections witnessed by the local user
    int numberOfWitnessedUserWithReadOnlyAccessSelections = 0;
    int numberOfGestures = 0;

    /**
     * iterate through SelectionEvens caused by users with {@link User.Permission#READONLY_ACCESS}
     * and check if they occurred within the file the local user was viewing and check if the
     * gestured flag was set for this selection.
     */
    for (SelectionEvent currentEntry : userWithReadOnlyAccessSelectionEvents) {
      if (currentEntry.withinFile) {
        numberOfWitnessedUserWithReadOnlyAccessSelections++;
      }
      if (currentEntry.gestured) {
        numberOfGestures++;
      }
    }

    data.put(
        KEY_TOTAL_USER_WITH_READONLY_ACCESS_SELECTION_COUNT,
        userWithReadOnlyAccessSelectionEvents.size());

    data.put(KEY_GESTURE_COUNT, numberOfGestures);
    data.put(
        KEY_WITNESSED_USER_WITH_READONLY_ACCESS_SELECTION_COUNT,
        numberOfWitnessedUserWithReadOnlyAccessSelections);
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    editorManager.addSharedEditorListener(editorListener);
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    editorManager.removeSharedEditorListener(editorListener);
  }
}
