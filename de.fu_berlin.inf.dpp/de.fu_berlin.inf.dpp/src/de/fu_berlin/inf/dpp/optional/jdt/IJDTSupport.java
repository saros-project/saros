package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.List;

import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;

public interface IJDTSupport {

    /**
     * Will return all {@link IPreferenceManipulator}s which are available for
     * the JDT plug-in.
     */
    public List<IPreferenceManipulator> getPreferenceManipulators();

}
