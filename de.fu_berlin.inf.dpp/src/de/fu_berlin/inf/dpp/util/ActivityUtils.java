package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;

/**
 * Class contains static helper methods for {@link IActivityDataObject ADOs}.
 */
public class ActivityUtils {

    /**
     * Checks if the give collections contains only
     * {@linkplain ChecksumActivityDataObject checksum ADOs}.
     * 
     * @param activities
     *            collection containing {@linkplain IActivityDataObject ADOs}
     * @return <code>true</code> if the collection contains only checksum ADOs,
     *         <code>false</code> otherwise
     */
    public static boolean containsChecksumsOnly(
        Collection<IActivityDataObject> activities) {

        if (activities.isEmpty())
            return false;

        for (IActivityDataObject a : activities)
            if (!(a instanceof ChecksumActivityDataObject))
                return false;

        return true;
    }

    /**
     * Tries to reduce the number of {@link IActivityDataObject ADOs} so that:
     * <p>
     * 
     * <pre>
     * for (activity : optimize(activities))
     *         exec(activity)
     * 
     * will produce the same result as
     * 
     * for (activity : activities)
     *         exec(activity)
     * </pre>
     * 
     * @param activities
     *            a collection containing the ADOs to optimize
     * @return a list which may contains a reduced amount of ADOs
     */

    public static List<IActivityDataObject> optimize(
        Collection<IActivityDataObject> activities) {

        List<IActivityDataObject> result = new ArrayList<IActivityDataObject>(
            activities.size());

        boolean[] dropDAOIdx = new boolean[activities.size()];

        Map<SPathDataObject, Integer> selections = new HashMap<SPathDataObject, Integer>();
        Map<SPathDataObject, Integer> viewports = new HashMap<SPathDataObject, Integer>();

        /*
         * keep only the latest selection/viewport activities per project and
         * path
         */

        int daoIdx = 0;

        for (IActivityDataObject dao : activities) {

            if (dao instanceof TextSelectionActivityDataObject) {

                SPathDataObject daoPath = ((TextSelectionActivityDataObject) dao)
                    .getPath();

                Integer idx = selections.get(daoPath);

                if (idx != null)
                    dropDAOIdx[idx] = true;

                selections.put(daoPath, daoIdx);
            } else if (dao instanceof ViewportActivityDataObject) {
                SPathDataObject daoPath = ((ViewportActivityDataObject) dao)
                    .getPath();

                Integer idx = viewports.get(daoPath);

                if (idx != null)
                    dropDAOIdx[idx] = true;

                viewports.put(daoPath, daoIdx);
            }

            daoIdx++;
        }

        daoIdx = 0;

        for (IActivityDataObject dao : activities)
            if (!dropDAOIdx[daoIdx++])
                result.add(dao);

        return result;
    }
}
