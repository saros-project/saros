package de.fu_berlin.inf.dpp.context;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import org.eclipse.core.runtime.AssertionFailedException;
import org.junit.Test;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.internal.IBBTransport;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.util.EclipseHelper;
import de.fu_berlin.inf.dpp.util.EclipseHelperTestSaros;

/**
 * @author cordes
 */
public class SarosContextTest {

    @Test
    public void testInitComponent() {
        SarosContext context = SarosContext.getContextForSaros(new TestSaros())
            .isTestContext().build();

        TestEntity entity = new TestEntity();
        context.initComponent(entity);

        assertNotNull(entity.sarosSessionManager);
        assertEquals(context.getComponent(SarosSessionManager.class),
            entity.sarosSessionManager);
    }

    @Test
    public void testInitWithSuperConstructor() {
        SarosContext context = SarosContext.getContextForSaros(new TestSaros())
            .isTestContext().build();

        TestEntityCallsSuperConstructor testEntityCallsSuperConstructor = new TestEntityCallsSuperConstructor(
            context);

        assertNotNull(testEntityCallsSuperConstructor.sarosSessionManager);
        assertEquals(context.getComponent(SarosSessionManager.class),
            testEntityCallsSuperConstructor.sarosSessionManager);
    }

    @Test
    public void testMultipleInstances() {
        SarosContext context1 = SarosContext
            .getContextForSaros(new TestSaros()).isTestContext().build();
        SarosContext context2 = SarosContext
            .getContextForSaros(new TestSaros()).isTestContext().build();

        assertNotSame(context1.getComponent(SarosSessionManager.class),
            context2.getComponent(SarosSessionManager.class));
        assertNotSame(context1.getComponent(Saros.class), context2
            .getComponent(Saros.class));
    }

    @Test
    public void testReinject() {
        SarosContext context = SarosContext.getContextForSaros(new TestSaros())
            .isTestContext().build();
        assertNull(context.getComponent(TestEntity.class));

        TestEntity entity = new TestEntity();
        assertNull(entity.sarosSessionManager);

        context.reinject(entity);

        assertNotNull(context.getComponent(TestEntity.class));
        assertNotNull(entity.sarosSessionManager);
        assertEquals(context.getComponent(SarosSessionManager.class),
            entity.sarosSessionManager);

        TestEntity entityFromContext = context.getComponent(TestEntity.class);
        assertEquals(entityFromContext, entity);
    }

    @Test
    public void testUsageOfTestContextInSarosPluginContext() {
        SarosContext context = SarosContext.getContextForSaros(new TestSaros())
            .isTestContext().build();

        SarosPluginContext.setSarosContext(context);

        TestEntity entity = new TestEntity();

        try {
            SarosPluginContext.initComponent(entity);
            fail("We expected a RuntimeException from SarosPluginContext!");
        } catch (Exception e) {
            // we make an explicit test of the thrown exception because
            // otherwise we cannot make sure it's the right RuntimeException.
            assertTrue(e.getMessage().contains(
                "You want to initiliaze a component with a test-context!"));
        }
    }

    @Test
    public void testEclipseHelper() {
        // testcontext
        {
            TestSaros testSaros = new TestSaros();
            testSaros.getPreferenceStore().setValue(PreferenceConstants.USERNAME, "Bob");

            SarosContext testContext = SarosContext.getContextForSaros(testSaros).isTestContext().build();
            assertTrue(testContext.getComponent(EclipseHelper.class) instanceof EclipseHelperTestSaros);
            String stateLocationPath = testContext.getComponent(EclipseHelper.class).getStateLocation().toPortableString();
            assertEquals(stateLocationPath, "test/resources/states/Bob");
        }
        // livecontext
        {
            SarosContext liveContext = SarosContext.getContextForSaros(new TestSaros()).build();
            try {
                String stateLocationPath = liveContext.getComponent(EclipseHelper.class).getStateLocation().toPortableString();
                assertEquals(stateLocationPath, "test/resources/states/Bob");
                fail("assert AssertionFailedException: application has not been initialized.");
            } catch (AssertionFailedException exception) {
                assertEquals("assertion failed: The application has not been initialized.", exception.getMessage());
            }

            try {
                liveContext.getComponent(EclipseHelper.class).getWorkspace();
                fail("assert IllegalStateException: Workspace is closed.");
            } catch (IllegalStateException exception) {
                assertEquals("Workspace is closed.", exception.getMessage());
            }
        }
    }

    @Test
    public void ensureIBBTransportInContext() {
        SarosContext context1 = SarosContext
            .getContextForSaros(new TestSaros()).isTestContext().build();
        assertNotNull(context1.getComponent(IBBTransport.class));
        SarosContext context2 = SarosContext
            .getContextForSaros(new TestSaros()).isTestContext().build();
        assertNotNull(context2.getComponent(IBBTransport.class));

        // it's a singleton ...
        assertTrue(context1.getComponent(IBBTransport.class) == context1
            .getComponent(IBBTransport.class));

        // different contexts different instances ...
        assertTrue(context1.getComponent(IBBTransport.class) != context2
            .getComponent(IBBTransport.class));
    }

    private class TestEntity {
        @Inject
        protected SarosSessionManager sarosSessionManager;
    }

    private class TestEntityWithPrivateConstructor {
        private TestEntityWithPrivateConstructor(SarosContext context) {
            context.initComponent(this);
        }
    }

    private class TestEntityCallsSuperConstructor extends
        TestEntityWithPrivateConstructor {
        @Inject
        protected SarosSessionManager sarosSessionManager;

        private TestEntityCallsSuperConstructor(SarosContext context) {
            super(context);
        }
    }
}
