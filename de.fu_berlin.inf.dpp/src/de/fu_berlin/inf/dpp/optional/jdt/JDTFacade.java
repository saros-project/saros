package de.fu_berlin.inf.dpp.optional.jdt;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.IPreferenceManipulator;
import de.fu_berlin.inf.dpp.project.SessionManager;

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
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class JDTFacade implements IJDTSupport {

    public static final Logger log = Logger
        .getLogger(JDTFacade.class.getName());

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

    public void installSharedDocumentProvider(SessionManager sessionManager) {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        jdtSupport.installSharedDocumentProvider(sessionManager);
    }

    public IDocumentProvider getDocumentProvider() {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        return jdtSupport.getDocumentProvider();
    }

    public List<IPreferenceManipulator> getPreferenceManipulators() {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        return jdtSupport.getPreferenceManipulators();
    }
}
