package de.fu_berlin.inf.dpp.project;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;

/**
 * A ResourceActivityFilter stores pending activities for the
 * {@link SharedResourcesManager}, then orders and filters them.
 */
class ResourceActivityFilter {
    private List<IResourceActivity> enteredActivities = new ArrayList<IResourceActivity>();

    public void enterAll(List<? extends IResourceActivity> activities) {
        enteredActivities.addAll(activities);
    }

    public void enter(IResourceActivity activity) {
        enteredActivities.add(activity);
    }

    public boolean isEmpty() {
        return enteredActivities.isEmpty();
    }

    /**
     * Filters and returns the activities in order and clears the list of stored
     * activities. The order is: folder creations, file activities, folder
     * removals, other resource activities.
     */
    public List<IResourceActivity> retrieveAll() {
        /*
         * haferburg: Sorting is not necessary, because activities are already
         * sorted enough (activity on parent comes before activity on child).
         * All we need to do is make sure that folders are created first and
         * deleted last. The sorting stuff was introduced with 1742 (1688).
         */
        List<IResourceActivity> fileActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> otherActivities = new ArrayList<IResourceActivity>();

        // Split all collectedActivities.
        for (IResourceActivity activity : enteredActivities) {
            if (activity instanceof FileActivity) {
                fileActivities.add(activity);
            } else if (activity instanceof FolderCreatedActivity) {
                folderCreateActivities.add(activity);
            } else if (activity instanceof FolderDeletedActivity) {
                folderRemoveActivities.add(activity);
            } else {
                otherActivities.add(activity);
            }
        }

        // Add activities to the result.
        List<IResourceActivity> result = new ArrayList<IResourceActivity>();
        result.addAll(folderCreateActivities);
        result.addAll(fileActivities);
        result.addAll(folderRemoveActivities);
        result.addAll(otherActivities);

        enteredActivities.clear();

        return result;
    }

}