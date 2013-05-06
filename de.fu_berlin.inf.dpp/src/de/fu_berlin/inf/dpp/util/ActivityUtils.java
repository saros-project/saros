package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity.Type;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.TimedActivityDataObject;

/**
 * Class contains static helper methods on activities
 */
public class ActivityUtils {

    /**
     * Checks if all {@link TimedActivityDataObject}s in the given Collection
     * are of instance {@link ChecksumActivityDataObject}
     * 
     * @param timedActivities
     *            Collection of {@link ChecksumActivityDataObject}s
     * @return true, if only {@link ChecksumActivityDataObject}s are in the
     *         given collection, false otherwise
     */
    public static boolean containsChecksumsOnly(
        List<TimedActivityDataObject> timedActivities) {

        for (TimedActivityDataObject a : timedActivities)
            if (!(a.getActivity() instanceof ChecksumActivityDataObject))
                return false;
        return true;
    }

    /**
     * Checks if all {@link IActivityDataObject}s in the given Collection are of
     * instance {@link ViewportActivityDataObject},
     * {@link JupiterActivityDataObject},
     * {@link TextSelectionActivityDataObject}, or
     * {@link TextEditActivityDataObject} - activities that can be created by
     * the inviter during a project synchronization and are uncritically to
     * delay (during an IBB transfer).
     * 
     * @param activities
     *            Collection of {@link IActivityDataObject}s
     * @return true, if all {@link IActivityDataObject}s are instances of
     *         mentioned activities, false otherwise
     */
    public static boolean containsQueueableActivitiesOnly(
        List<IActivityDataObject> activities) {

        for (IActivityDataObject a : activities)
            if (a instanceof ViewportActivityDataObject
                || a instanceof JupiterActivityDataObject
                || a instanceof TextSelectionActivityDataObject
                || a instanceof TextEditActivityDataObject)
                continue;
            else
                return false;

        return true;
    }

    /**
     * This method tries to reduce the number of activityDataObjects transmitted
     * by removing activityDataObjects that would overwrite each other and
     * joining activityDataObjects that can be send as a single
     * activityDataObject.
     */
    public static List<IActivityDataObject> optimize(
        List<IActivityDataObject> toOptimize) {

        List<IActivityDataObject> result = new ArrayList<IActivityDataObject>(
            toOptimize.size());

        TextSelectionActivityDataObject selection = null;
        LinkedHashMap<SPathDataObject, ViewportActivityDataObject> viewport = new LinkedHashMap<SPathDataObject, ViewportActivityDataObject>();

        for (IActivityDataObject activityDataObject : toOptimize) {

            if (activityDataObject instanceof TextEditActivityDataObject) {
                TextEditActivityDataObject textEdit = (TextEditActivityDataObject) activityDataObject;
                textEdit = joinTextEdits(result, textEdit);
                result.add(textEdit);
            } else if (activityDataObject instanceof TextSelectionActivityDataObject) {
                selection = (TextSelectionActivityDataObject) activityDataObject;
            } else if (activityDataObject instanceof ViewportActivityDataObject) {
                ViewportActivityDataObject viewActivity = (ViewportActivityDataObject) activityDataObject;
                viewport.remove(viewActivity.getPath());
                viewport.put(viewActivity.getPath(), viewActivity);
            } else if (activityDataObject instanceof FolderActivityDataObject) {
                FolderActivityDataObject folderEdit = (FolderActivityDataObject) activityDataObject;
                foldRecursiveDelete(result, folderEdit);
            } else {
                result.add(activityDataObject);
            }
        }

        // only send one selection activityDataObject
        if (selection != null)
            result.add(selection);

        // Add only one viewport per editor
        for (Entry<SPathDataObject, ViewportActivityDataObject> entry : viewport
            .entrySet()) {
            result.add(entry.getValue());
        }

        assert !result.contains(null);

        return result;
    }

    private static void foldRecursiveDelete(List<IActivityDataObject> result,
        FolderActivityDataObject folderEdit) {

        if (folderEdit.getType() != Type.Removed) {
            result.add(folderEdit);
            return;
        }

        int i = result.size() - 1;
        boolean dropNew = false;

        while (i >= 0 && !dropNew) {
            FolderActivityDataObject curr = null;

            if (result.get(i) instanceof FolderActivityDataObject)
                curr = (FolderActivityDataObject) result.get(i);
            else {
                i--;
                continue;
            }

            if (curr.isChildOf(folderEdit))
                result.remove(i);

            else if (curr.getPath().equals(folderEdit.getPath())) {
                result.remove(i);
                dropNew = true;
            }

            i--;
        }

        if (!dropNew)
            result.add(folderEdit);
    }

    private static TextEditActivityDataObject joinTextEdits(
        List<IActivityDataObject> result, TextEditActivityDataObject textEdit) {
        if (result.size() == 0) {
            return textEdit;
        }

        IActivityDataObject lastActivity = result.get(result.size() - 1);
        if (lastActivity instanceof TextEditActivityDataObject) {
            TextEditActivityDataObject lastTextEdit = (TextEditActivityDataObject) lastActivity;

            if (((lastTextEdit.getSource() == null) || lastTextEdit.getSource()
                .equals(textEdit.getSource()))
                && (textEdit.getOffset() == lastTextEdit.getOffset()
                    + lastTextEdit.getText().length())) {
                result.remove(lastTextEdit);
                textEdit = new TextEditActivityDataObject(
                    lastTextEdit.getSource(),
                    lastTextEdit.getOffset(),
                    lastTextEdit.getText() + textEdit.getText(),
                    lastTextEdit.getReplacedText() + textEdit.getReplacedText(),
                    lastTextEdit.getPath());
            }
        }

        return textEdit;
    }

}
