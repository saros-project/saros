package de.fu_berlin.inf.dpp.optional.cdt;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * 
 * Class which hides access to the CDT Plugin functionality, so that Saros only
 * optionally dependes on CDT.
 * 
 * To achieve this, CDTFacade implements the ICDTSupport interface but only
 * forwards calls. The interface is implemented using the CDT in CDTSupport to
 * which the calls are forwarded if the CDT is available.
 * 
 * To make this work, no static dependencies to CDTSupport may exist.
 * 
 * @author oezbek
 */
@Component(module = "integration")
public class CDTFacade implements ICDTSupport {

    public static final Logger log = Logger
        .getLogger(CDTFacade.class.getName());

    public ICDTSupport cdtSupport;

    public boolean isCDTAvailable() {
        return cdtSupport != null;
    }

    public CDTFacade() {

        // Check for CDT Bundle
        if (Platform.getBundle("org.eclipse.cdt.ui") != null
            && Platform.getBundle("org.eclipse.cdt.core") != null) {
            // We cannot just create a new instance, because this instance
            // cannot be created if CDT is missing.
            try {
                cdtSupport = (ICDTSupport) Class.forName(
                    "de.fu_berlin.inf.dpp.optional.cdt.CDTSupport")
                    .newInstance();
            } catch (Throwable t) {
                // We were unable to enable the CDT-Support
                log.warn("ICDTSupport could not be instantiated,"
                    + " despite CDT Plugin being installed");
            }
        }
    }

    public void installSharedDocumentProvider(SessionManager sessionManager) {

        if (!isCDTAvailable())
            throw new IllegalStateException("CDT Plugin is not available");

        cdtSupport.installSharedDocumentProvider(sessionManager);
    }

    public IDocumentProvider getDocumentProvider() {

        if (!isCDTAvailable())
            throw new IllegalStateException("CDT Plugin is not available");

        return cdtSupport.getDocumentProvider();
    }
}
