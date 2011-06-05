package de.fu_berlin.inf.dpp.util;

import org.eclipse.core.resources.IWorkspace;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.context.TestSaros;
import de.fu_berlin.inf.dpp.util.EclipseHelper;

/**
 * @author cordes
 */
public class EclipseHelperTestSaros extends EclipseHelper {

    @Inject
    protected Saros saros;

    @Override
    public IWorkspace getWorkspace() {
        TestSaros testsaros = (TestSaros) saros;
        return testsaros.getWorspace();
    }
}
