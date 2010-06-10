package de.fu_berlin.inf.dpp.vcs.testing;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

@Component(module = "ui")
public class TestSVNAction implements IWorkbenchWindowActionDelegate {

    private static final Logger log = Logger.getLogger(TestSVNAction.class
        .getName());

    protected IWorkbenchWindow window;

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected PreferenceUtils preferenceUtils;

    /**
     * This class is instantiated by Eclipse to hook it up in the Menu bar.
     */
    public TestSVNAction() {
        Saros.reinject(this);
    }

    /**
     * @review runSafe OK
     */
    public void run(IAction action) {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runTestSVN();
            }
        });
    }

    public void runTestSVN() {
        SVNTest.testCheckout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // We don't need to update on a selectionChanged
    }

    public void dispose() {
        // Nothing to dispose
    }

}
