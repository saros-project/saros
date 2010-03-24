package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;

/**
 * Implementation of IJDTSupport which really uses JDT functionality.
 * 
 * All access to this code should go through JDTFacade.
 * 
 * @author oezbek
 * 
 * @noinstantiate Do not refer to this class directly or Saros will crash if a
 *                user has JDT not installed. This class is created using
 *                reflection from JDTFacade if and only if the JDT Plugin is
 *                available
 */
class JDTSupport implements IJDTSupport {

    public List<IPreferenceManipulator> getPreferenceManipulators() {
        List<IPreferenceManipulator> result = new ArrayList<IPreferenceManipulator>();

        result.add(new SaveActionConfigurator());

        return result;
    }

}
