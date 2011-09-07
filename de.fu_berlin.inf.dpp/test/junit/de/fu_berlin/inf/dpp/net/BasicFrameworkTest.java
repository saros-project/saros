package de.fu_berlin.inf.dpp.net;

import de.fu_berlin.inf.dpp.context.TestSaros;
import de.fu_berlin.inf.dpp.test.xmpp.XmppUser;
import de.fu_berlin.inf.dpp.util.AbstractSarosUnitTest;
import de.fu_berlin.inf.dpp.util.EclipseHelper;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author cordes
 */
public class BasicFrameworkTest extends AbstractSarosUnitTest {

    @Test
    public void testXmppConnection() {
        TestSaros saros = getTestSaros();
        saros.connect(false);
        assertTrue(saros.getSarosNet().isConnected());
    }

    @Test
    public void testWorkspace() {
        TestSaros saros = getTestSaros();
        assertNotNull(saros.getWorkspace());
        assertNotNull(saros.getContext().getComponent(EclipseHelper.class).getWorkspace());
        assertEquals(1, saros.getWorkspace().getRoot().getProjects().length);
        assertEquals("alice", saros.getUserName());
    }
}
