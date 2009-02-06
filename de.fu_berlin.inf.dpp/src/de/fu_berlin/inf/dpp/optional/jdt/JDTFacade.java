package de.fu_berlin.inf.dpp.optional.jdt;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * 
 * Class which hides access to the JDT Plugin functionality, so that Saros only
 * optionally dependes on JDT.
 * 
 * To achieve this, JDTFacade implements the IJDTSupport interface but only
 * forwards calls. The interface is implemented using the JDT in JDTSupport to
 * which the calls are forwarded if the JDT is available.
 * 
 * To make this work, no static dependencies to JDTSupport may exist.
 * 
 * @author oezbek
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
            } catch (Exception e) {
                // We were unable to enable the JDT-Support
                log
                    .warn("IJDTSupport could not be instantiated, despite JDT Plugin being installed");
            }
        }
    }

    public void installSharedDocumentProvider() {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        jdtSupport.installSharedDocumentProvider();
    }

    public IDocumentProvider getDocumentProvider() {

        if (!isJDTAvailable())
            throw new IllegalStateException("JDT Plugin is not available");

        return jdtSupport.getDocumentProvider();
    }
}
