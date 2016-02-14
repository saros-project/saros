package de.fu_berlin.inf.dpp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.EclipseMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareCoreComponents;
import de.fu_berlin.inf.dpp.test.mocks.PrepareEclipseComponents;

/**
 * Check {@link SarosEclipseContextFactory} for internal integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Saros.class })
@MockPolicy({ PrepareCoreComponents.class, PrepareEclipseComponents.class })
public class SarosEclipseContextFactoryTest {

    private MutablePicoContainer container;
    private Saros saros;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock Saros/Core components
        ContextMocker.addMocksFromFactory(container,
            new SarosCoreContextFactory());

        // mock Saros environment
        saros = EclipseMocker.mockSaros();

        // mock Eclipse dependencies
        EclipseMocker.mockResourcesPlugin();
        EclipseMocker.mockPlatform();
    }

    @Test
    public void testCreateComponents() {
        ISarosContextFactory factory = new SarosEclipseContextFactory(saros);

        factory.createComponents(container);
        Assert.assertNotNull(container.getComponents());
    }
}
