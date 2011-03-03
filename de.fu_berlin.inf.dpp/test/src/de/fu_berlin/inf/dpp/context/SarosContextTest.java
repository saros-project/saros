package de.fu_berlin.inf.dpp.context;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import org.junit.Test;
import org.picocontainer.annotations.Inject;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

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
