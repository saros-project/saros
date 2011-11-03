package de.fu_berlin.inf.dpp.net;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.context.TestSaros;
import de.fu_berlin.inf.dpp.util.AbstractSarosUnitTestCase;
import de.fu_berlin.inf.dpp.util.EclipseHelper;

/**
 * @author cordes
 */
public class BasicFrameworkTest extends AbstractSarosUnitTestCase {

    @Test
    @Ignore("Nobody using the framework, so disabled for now")
    public void testXmppConnection() {
        TestSaros saros = getTestSaros();
        saros.connect(false);
        assertTrue(saros.getSarosNet().isConnected());
    }

    @Test
    @Ignore("Nobody using the framework, so disabled for now")
    public void testWorkspace() {
        TestSaros saros = getTestSaros();
        assertNotNull(saros.getWorkspace());
        assertNotNull(saros.getContext().getComponent(EclipseHelper.class)
            .getWorkspace());
        assertEquals(1, saros.getWorkspace().getRoot().getProjects().length);
        assertEquals("alice", saros.getUserName());
    }
}
