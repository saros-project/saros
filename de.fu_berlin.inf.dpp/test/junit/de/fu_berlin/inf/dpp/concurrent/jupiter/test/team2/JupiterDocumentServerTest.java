package de.fu_berlin.inf.dpp.concurrent.jupiter.test.team2;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;

public class JupiterDocumentServerTest extends TestCase {

    private static final String PATH = "TestPath";

    IProject project;
    IPath path = new Path(PATH);

    @Override
    public @Before
    void setUp() throws Exception {
        project = createMock(IProject.class);
        replay(project);
    }

    /*
     * Info: there should be added a Spec. for JupiterDocumentServer
     */
    @Test
    public void testServerAndSPath() {

        // Create SPath without Project
        try {
            new SPath(null, path);
            fail(); // there has to be a project for a spath
        } catch (IllegalArgumentException e) {
            // expected
        }

        SPath p = new SPath(project, path);
        assertEquals(path, p.getProjectRelativePath());

        JupiterDocumentServer j = new JupiterDocumentServer(p);

        // Add JID
        JID jid = new JID("alice@saros.com");
        j.addProxyClient(jid);
        // it should exist not
        assertTrue(j.isExist(jid));

        j.reset(jid);
        assertTrue(j.isExist(jid));

        // add it again
        j.addProxyClient(jid);

        j.removeProxyClient(jid);

        // should not be there anymore after removing
        assertFalse(j.isExist(jid));

        // and resetting should not work
        j.reset(jid);
        assertFalse(j.isExist(jid));

    }
}
