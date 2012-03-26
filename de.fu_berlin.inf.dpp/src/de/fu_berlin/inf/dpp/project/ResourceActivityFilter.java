package de.fu_berlin.inf.dpp.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;

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
     * activities. The order is: VCS activities, folder creations, file
     * activities, folder removals.
     */
    public List<IResourceActivity> retrieveAll() {
        /*
         * haferburg: Sorting is not necessary, because activities are already
         * sorted enough (activity on parent comes before activity on child).
         * All we need to do is make sure that folders are created first and
         * deleted last. The sorting stuff was introduced with 1742 (1688).
         */
        List<IResourceActivity> vcsActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> fileActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> otherActivities = new ArrayList<IResourceActivity>();

        // Split all collectedActivities.
        for (IResourceActivity activity : enteredActivities) {

            if (activity instanceof VCSActivity) {
                vcsActivities.add(activity);
            } else if (activity instanceof FileActivity) {
                fileActivities.add(activity);
            } else if (activity instanceof FolderActivity) {
                FolderActivity.Type tFolder = ((FolderActivity) activity)
                    .getType();
                if (tFolder == FolderActivity.Type.Created)
                    folderCreateActivities.add(activity);
                else if (tFolder == FolderActivity.Type.Removed)
                    folderRemoveActivities.add(activity);
            } else {
                otherActivities.add(activity);
            }
        }
        /*
         * TODO This might be SVN specific. The SVN jobs update sync info on
         * child before parent, so by reversing the list we know: If i<j then
         * !vcsActivities.get(j).includes(vcsActivities.get(i)).
         */
        Collections.reverse(vcsActivities);

        // Add activities to the result.
        List<IResourceActivity> result = new ArrayList<IResourceActivity>();
        result.addAll(vcsActivities);
        result.addAll(folderCreateActivities);
        result.addAll(fileActivities);
        result.addAll(folderRemoveActivities);
        result.addAll(otherActivities);

        // Note: This iteration relies on result starting with
        // vcsActivities.
        for (int i = 0; i < vcsActivities.size(); i++) {
            VCSActivity vcsActivity = (VCSActivity) (vcsActivities.get(i));
            if (!result.contains(vcsActivity))
                continue;
            // Iterate in reverse order so that we can safely remove items.
            for (int j = result.size() - 1; j > i; j--) {
                IResourceActivity otherActivity = result.get(j);
                if (result.contains(otherActivity)
                    && vcsActivity.includes(otherActivity)) {
                    if (!(otherActivity instanceof VCSActivity)) {
                        vcsActivity.containedActivity.add(0, otherActivity);
                    }
                    SharedResourcesManager.log
                        .debug("Ignoring redundant activity " + otherActivity);
                    result.remove(j);
                }
            }
        }

        enteredActivities.clear();

        return result;
    }

}