package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;

/**
 * 
 * Class which hides access to the JDT plug-in functionality, so that Saros only
 * optionally depends on JDT.
 * 
 * To achieve this, JDTFacade implements the IJDTSupport interface but only
 * forwards calls. The interface is implemented using the JDT in JDTSupport to
 * which the calls are forwarded if the JDT is available.
 * 
 * To make this work, no static dependencies to JDTSupport may exist but rather
 * reflection is being used.
 * 
 * @author oezbek
 */
@Component(module = "integration")
public class JDTFacade implements IJDTSupport {

    private static final Logger log = Logger.getLogger(JDTFacade.class);

    public IJDTSupport jdtSupport;

    public boolean isJDTAvailable() {
        return jdtSupport != null;
    }

    public JDTFacade() {

        // Check for JDT Bundle
        if (Platform.getBundle("org.eclipse.jdt.ui") != null
            && Platform.getBundle("org.eclipse.jdt.core") != null) {
            // We cannot just create a new instance, because this instance
            // cannot be created if JDT is missing.
            try {
                jdtSupport = (IJDTSupport) Class.forName(
                    "de.fu_berlin.inf.dpp.optional.jdt.JDTSupport")
                    .newInstance();
            } catch (Throwable t) {
                // We were unable to enable the JDT-Support
                log
                    .warn("IJDTSupport could not be instantiated, despite JDT Plugin being installed");
            }
        }
    }

    public List<IPreferenceManipulator> getPreferenceManipulators() {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        return jdtSupport.getPreferenceManipulators();
    }
}
