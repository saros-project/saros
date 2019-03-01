package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A collector class that collects local TextEditActivitys and compares them in relation to
 * parallelism or rather concurrency with remote text events.
 *
 * <p>It is measured how many characters the local user wrote in a session (whitespaces are omitted,
 * because Eclipse produces many of them automatically e.g. when a new line is started), how many
 * TextEditActivitys he produced (which can be different to the number of characters he wrote, e.g.
 * when copy&paste or Eclipse's method generation was used) and how concurrent the local user's
 * writing was to remote users using different sample intervals.
 *
 * <p>Furthermore, it accumulates the characters edited for all remote participants.
 *
 * <p>A little addition was made to track possible paste / auto generations. If a single text edit
 * activity produces more than a certain number (pasteThreshold) of characters, it will be counted
 * as possible paste action. The threshold was chosen to be 16 due to some testing. The characters
 * within a textEditActivity heavily depends on the connection speed. When a the local user fires
 * several textEdits within a short interval he might see those edits as several single edits with a
 * few characters but a user will most likely get less textEditActivities containing a bunch of
 * characters. The manual tests results were: When "typing" (rather thrashing the keyboard) as fast
 * as one could, the user received text edits with up to 20 characters. As this typing speed is
 * beyond human capabilities, I've decided to set the threshold to 16 which will most likely prevent
 * getting false positives (e.g. a really fast typer is typing instead of a paste). Unfortunately,
 * this relatively high threshold will cause the collector to slip some true pastes as well. (E.g.
 * an auto completion of a comment block)
 *
 * <p>NOTE: TextEditActivitys that are triggered by Eclipse (e.g. when restoring an editor) are
 * counted as well. And refactorings can produce quite a large number of characters that are
 * counted. <br>
 *
 * <p>Fixed: The number of non parallel edits was set to 100% if there are no concurrent edits,
 * which is fine. But if there are no edits at all, the non parallel edits are still set to 100%
 * even though this should rather be 0%.
 *
 * <p>Example:<br>
 * The percent numbers (for all intervals + non-parallel) should add up to (nearly) 100, slight
 * rounding errors are possible. The counted chars for all intervals and non-parallel edits should
 * add up to textedits.chars <code>
 * textedits.chars=5 <br>
 * textedits.pastes.chars=0 <br>
 * textedits.pastes=0 <br>
 * textedits.nonparallel.chars=1 <br>
 * textedits.nonparallel.percent=20 <br>
 * textedits.parallel.interval.1.chars=1 <br>
 * textedits.parallel.interval.1.count=1 <br>
 * textedits.parallel.interval.1.percent=20 <br>
 * textedits.parallel.interval.10.chars=2 <br>
 * textedits.parallel.interval.10.count=2 <br>
 * textedits.parallel.interval.10.percent=40 <br>
 * textedits.parallel.interval.2.chars=1 <br>
 * textedits.parallel.interval.2.count=1 <br>
 * textedits.parallel.interval.2.percent=20<br>
 * textedits.remote.user.1.chars=1<br>
 * textedits.remote.user.1.pastes.chars=0<br>
 * textedits.remote.user.1.pastes=0<br>
 * </code>
 */
/*
 *
 * TODO: synchronize the numbers the users get with the other collectors (E.g.
 * Alice as user should appear as user.2 in all statistic fields.
 *
 * @author Lisa Dohrmann, Moritz von Hoffen
 */
@Component(module = "feedback")
public class TextEditCollector extends AbstractStatisticCollector {

  private static final Logger log = Logger.getLogger(TextEditCollector.class);

  private static final String KEY_PERCENT = "percent";

  private static final String KEY_CHARS = "chars";
  private static final String KEY_PASTES = "pastes";
  private static final String KEY_COUNT = "count";

  private static final String KEY_NON_PARALLEL_TEXT_EDITS = "textedits.nonparallel";
  private static final String KEY_PARALLEL_TEXT_EDITS = "textedits.parallel.interval";

  private static final String KEY_REMOTE_USER = "textedits.remote.user";
  private static final String KEY_LOCAL_USER = "textedits.local";

  private static class EditEvent {
    private long time;
    private int chars;

    public EditEvent(long time, int chars) {
      this.time = time;
      this.chars = chars;
    }
  }

  /**
   * Different sample intervals (in milliseconds) for measuring parallel text edits. It is
   * determined for each local text edit if there occurred a remote text edit X seconds before or
   * after
   */
  private static final int[] sampleIntervals = {1000, 2000, 5000, 10000, 15000};

  private long charsWritten = 0;

  /**
   * This threshold is the upper limit that is believed to have been produced by "hand". If a single
   * text edit activity contains more characters than specified by this threshold it indicates that
   * a paste action has occurred. That paste action could either mean an auto completion /
   * generation using eclipse or a simple paste from the clip board.
   */
  private int pasteThreshold = 16;

  private User localUser = null;

  /** List to contain local {@link EditEvent}s */
  private final List<EditEvent> localEvents =
      Collections.synchronizedList(new ArrayList<EditEvent>());

  /** List to contain remote {@link EditEvent}s */
  private final List<EditEvent> remoteEvents =
      Collections.synchronizedList(new ArrayList<EditEvent>());

  /** Maps sample interval to chars written in this interval */
  private final Map<Integer, Integer> parallelTextEdits = new HashMap<Integer, Integer>();
  /** Maps sample interval to number of edits in this interval */
  private final Map<Integer, Integer> parallelTextEditsCount = new HashMap<Integer, Integer>();

  /** A map which should possible detect auto generation and paste actions */
  private final Map<User, Integer> pastes = new HashMap<User, Integer>();

  /** A map which accumulates the characters produced within paste actions */
  private final Map<User, Integer> pastesCharCount = new HashMap<User, Integer>();

  /** for each key {@link User} an Integer is stored which represents the total chars edited. */
  private final Map<User, Integer> remoteCharCount = new HashMap<User, Integer>();

  private final IEditorManager editorManager;

  private final ISharedEditorListener editorListener =
      new ISharedEditorListener() {

        @Override
        public void textEdited(TextEditActivity textEdit) {
          User user = textEdit.getSource();
          String text = textEdit.getText();

          /*
           * delete whitespaces from the text because we don't want to count
           * them. that would result in quite a number of counted characters
           * the user actually hasn't written, e.g. when eclipse automatically
           * starts lines with tabs or spaces
           */
          int textLength = StringUtils.deleteWhitespace(text).length();

          EditEvent event = new EditEvent(System.currentTimeMillis(), textLength);

          /*
           * if the edit activity text length exceeds the threshold for
           * possible pastes store this as a possible paste and file it for
           * the user who made that possible paste. Moreover, store the number
           * of characters that were "pasted" or auto generated.
           */
          if (textLength > pasteThreshold) {
            Integer currentPasteCount = pastes.get(user);
            if (currentPasteCount == null) {
              currentPasteCount = 0;
            }
            pastes.put(user, currentPasteCount + 1);

            Integer currentPasteChars = pastesCharCount.get(user);
            if (currentPasteChars == null) {
              currentPasteChars = 0;
            }
            pastesCharCount.put(user, currentPasteChars + textLength);
          }

          if (log.isTraceEnabled()) {
            log.trace(
                String.format(
                    "Received chars written from %s " + "(whitespaces omitted): %s [%s]",
                    user, textLength, StringEscapeUtils.escapeJava(text)));
          }

          if (textLength > 0) {
            if (user.isLocal()) {
              /*
               * accumulate the written chars of the local user and store
               * the time and text length of this Activity
               */
              addToCharsWritten(textLength);
              localEvents.add(event);
            } else {
              /*
               * store all remote text edits for future comparison. As
               * those text edits are remote it needs to be determined,
               * who made the edit and to increase the appropriate edited
               * character count. The total text edit count is increased
               * by one for each TextEditActivity received.
               */
              remoteEvents.add(event);
              Integer currentCharCount = remoteCharCount.get(user);
              if (currentCharCount == null) {
                currentCharCount = 0;
              }
              remoteCharCount.put(user, currentCharCount + textLength);
            }
          }
        }
      };

  public TextEditCollector(
      StatisticManager statisticManager, ISarosSession session, IEditorManager editorManager) {
    super(statisticManager, session);

    this.editorManager = editorManager;
  }

  private synchronized void addToCharsWritten(int chars) {
    charsWritten += chars;
  }

  private synchronized long getCharsWritten() {
    return charsWritten;
  }

  @Override
  protected void processGatheredData() {

    /*
     * a variable to distinguish between users and assign a number to each
     * of these users.
     */
    int userNumber = 1;

    // store C&P statistic for remote users

    for (Map.Entry<User, Integer> entry : remoteCharCount.entrySet()) {

      User currentId = entry.getKey();
      int charCount = entry.getValue();

      int pasteCount = 0;
      int pasteChars = 0;

      if (pastes.get(currentId) != null) {
        pasteCount = pastes.get(currentId);
        pasteChars = pastesCharCount.get(currentId);
      }

      storeRemoteUserTextEditsStatistic(userNumber, pasteCount, pasteChars, charCount);

      // increment userNumber (so that next remote peer gets a different
      // number)
      userNumber++;
    }

    final long totalCharsWritten = getCharsWritten();

    // store C&P statistic for local user

    int pasteCount = 0;
    int pasteChars = 0;

    if (pastes.get(localUser) != null) {
      pasteCount = pastes.get(localUser);
      pasteChars = pastesCharCount.get(localUser);
    }

    storeLocalUserTextEditsStatistic(pasteCount, pasteChars, totalCharsWritten);

    // generate and store parallel text edits

    // process(...) modifies localEvents collection !
    for (int interval : sampleIntervals) process(interval);

    // all in the localEvents list remaining events were non-parallel
    long nonParallelTextEdits = 0;

    for (EditEvent local : localEvents) nonParallelTextEdits += local.chars;

    storeNonParallelTextEditsStatistic(nonParallelTextEdits, totalCharsWritten);

    if (parallelTextEditsCount.isEmpty()) return;

    assert (parallelTextEdits.size() == parallelTextEditsCount.size());
    assert (parallelTextEdits.keySet().equals(parallelTextEditsCount.keySet()));

    final List<Integer> intervalRanges =
        Arrays.asList(parallelTextEdits.keySet().toArray(new Integer[0]));

    for (int intervalRange : intervalRanges) {
      storeParallelTextEditsStatistic(
          intervalRange,
          parallelTextEditsCount.get(intervalRange),
          parallelTextEdits.get(intervalRange),
          totalCharsWritten);
    }
  }

  /**
   * Processes the given interval width, i.e. the local and remote edit events are iterated until a
   * pair is found that fulfills the condition: <br>
   * <code>local.time is element of [lastRemote.time - intervalWidth,
   * lastRemote.time + intervalWidth].</code><br>
   * <br>
   * This local edit is than counted as a parallel one.
   *
   * @param intervalWidth the width of the interval to be considered
   */
  private void process(int intervalWidth) {

    // FIXME synchronized(remoteEvents) { synchronized(localEvents) { ...
    Iterator<EditEvent> remote = remoteEvents.iterator();
    EditEvent lastRemote = (remote.hasNext() ? remote.next() : null);

    for (Iterator<EditEvent> localIterator = localEvents.iterator(); localIterator.hasNext(); ) {
      EditEvent local = localIterator.next();

      /*
       * FIXME why is this done in the for loop when the iterator is only
       * obtained once ?! If this is intended either move the while loop
       * out of the for loop or correct this, which may lead to another
       * serious performance issue for very long sessions. O(n^2)
       * performance
       */
      // Skip all remote events too far in the past
      while (lastRemote != null && local.time > lastRemote.time + intervalWidth) {
        lastRemote = (remote.hasNext() ? remote.next() : null);
      }

      if (lastRemote != null && local.time >= lastRemote.time - intervalWidth) {
        /*
         * This local edit occurred inside the time frame
         * [lastRemote.time - intervalWidth, lastRemote.time +
         * intervalWidth]. Therefore count it as a parallel text edit
         * and remove it from the list, because we are done with it.
         */
        int intervalSeconds = (int) Math.round(intervalWidth / 1000.0);
        addToMap(parallelTextEdits, intervalSeconds, local.chars);
        addToMap(parallelTextEditsCount, intervalSeconds, 1);
        localIterator.remove();
      }
      /*
       * Else: This local edit is non-parallel for the current
       * intervalWidth but it might be parallel to a larger interval. Just
       * leave it in the map and accumulate all remaining events as
       * non-parallel at the end.
       */
    }
  }

  /**
   * Stores the given value for the given key in the map. If there is a previous value for this key,
   * the old value is added to the new one and the result is stored in the map.
   *
   * @param map
   * @param key
   * @param value
   */
  private static void addToMap(Map<Integer, Integer> map, Integer key, Integer value) {
    Integer oldValue = map.get(key);

    if (oldValue != null) {
      value += oldValue;
    }
    map.put(key, value);
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    editorManager.addSharedEditorListener(editorListener);
    // set local users JID at the beginning of the session
    localUser = sarosSession.getLocalUser();
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    editorManager.removeSharedEditorListener(editorListener);
  }

  private void storeRemoteUserTextEditsStatistic(
      int userNumber, int pasteCount, long pasteCharCount, long totalCharCount) {

    data.put(KEY_REMOTE_USER, totalCharCount, userNumber, KEY_CHARS);
    data.put(KEY_REMOTE_USER, pasteCount, userNumber, KEY_PASTES);
    data.put(KEY_REMOTE_USER, pasteCharCount, userNumber, KEY_PASTES, KEY_CHARS);
  }

  private void storeLocalUserTextEditsStatistic(
      int pasteCount, long pasteCharCount, long totalCharCount) {

    data.put(KEY_LOCAL_USER, totalCharCount, KEY_CHARS);
    data.put(KEY_LOCAL_USER, pasteCount, KEY_PASTES);
    data.put(KEY_LOCAL_USER, pasteCharCount, KEY_PASTES, KEY_CHARS);
  }

  private void storeParallelTextEditsStatistic(
      int intervalRange, int intervalTextEditCount, long intervalCharCount, long totalCharCount) {

    data.put(KEY_PARALLEL_TEXT_EDITS, intervalCharCount, intervalRange, KEY_CHARS);

    data.put(
        KEY_PARALLEL_TEXT_EDITS,
        getPercentage(intervalCharCount, totalCharCount),
        intervalRange,
        KEY_PERCENT);

    data.put(KEY_PARALLEL_TEXT_EDITS, intervalTextEditCount, intervalRange, KEY_COUNT);
  }

  private void storeNonParallelTextEditsStatistic(long localCharCount, long totalCharCount) {
    data.put(KEY_NON_PARALLEL_TEXT_EDITS, localCharCount, KEY_CHARS);

    data.put(
        KEY_NON_PARALLEL_TEXT_EDITS, getPercentage(localCharCount, totalCharCount), KEY_PERCENT);
  }
}
