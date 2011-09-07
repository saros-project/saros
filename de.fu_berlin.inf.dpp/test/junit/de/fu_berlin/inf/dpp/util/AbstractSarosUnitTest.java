package de.fu_berlin.inf.dpp.util;

import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.context.TestSaros;
import de.fu_berlin.inf.dpp.test.fakes.EclipseWorkspaceFakeFacade;
import de.fu_berlin.inf.dpp.test.xmpp.XMPPServerFacadeForTests;
import de.fu_berlin.inf.dpp.test.xmpp.XmppUser;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Extend a new unit-test from this class and you will get a needed
 * functionality to write tests which handles instances of
 * {@link de.fu_berlin.inf.dpp.context.TestSaros} to run
 * productive code.
 * <p/>
 * When you run such a SarosUnitTest automatically a XMPP-Server will
 * be started which will be used by the test-instances.
 *
 * @author cordes
 */
public abstract class AbstractSarosUnitTest {

    private static Logger LOG = Logger.getLogger(AbstractSarosUnitTest.class);

    private XMPPServerFacadeForTests xmppFacade;

    @BeforeClass
    public static void startServer() {
        LOG.debug("\n\n **** SarosUnitTest-Test: Starting XMPP-Server! ****");
        XMPPServerFacadeForTests.startServer();
        LOG.debug("\n\n **** SarosUnitTest-Test: XMPP-Server Started! ****");
    }

    @AfterClass
    public static void stopServer() {
        LOG.debug("\n\n **** SarosUnitTest-Test: Stopping XMPP-Server! ****");
        XMPPServerFacadeForTests.stopServer();
        LOG.debug("\n\n **** SarosUnitTest-Test: XMPP-Server Stopped! ****");
    }

    @Before
    public void setup() {
        EclipseWorkspaceFakeFacade.deleteWorkspaces();
        xmppFacade = new XMPPServerFacadeForTests();
    }

    @After
    public void tearDown() {
        xmppFacade.deleteAllCreatedAccounts();
        EclipseWorkspaceFakeFacade.deleteWorkspaces();
    }

    /**
     * will be later replaced by a separate class for generating different types of scenarios      
     */
    protected TestSaros getTestSaros() {
        TestSaros result = new TestSaros();

        XmppUser xmppUser = xmppFacade.getNextUser();
        result.setXMPPUser(xmppUser);

        IWorkspace workspace = EclipseWorkspaceFakeFacade.createWorkspace(xmppUser.getUsername());
        IProject project = workspace.getRoot().getProject("test");
        EclipseWorkspaceFakeFacade.addSomeProjectData(project);
        result.setWorkspace(workspace);

        return result;
    }

}

