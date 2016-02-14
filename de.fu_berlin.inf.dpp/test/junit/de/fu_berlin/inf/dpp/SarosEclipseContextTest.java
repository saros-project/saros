package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.List;

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
import de.fu_berlin.inf.dpp.test.mocks.PrepareEclipseComponents;
import de.fu_berlin.inf.dpp.ui.browser.EclipseHTMLUIContextFactory;

/**
 * Checks the Saros/E context for integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Saros.class })
@MockPolicy({ PrepareEclipseComponents.class })
public class SarosEclipseContextTest {

    private MutablePicoContainer container;
    private Saros saros;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock Eclipse dependencies
        EclipseMocker.mockResourcesPlugin();
        EclipseMocker.mockPlatform();

        // mock Saros environment
        saros = EclipseMocker.mockSaros();

        ContextMocker.addMock(container, ISarosContext.class);
    }

    @Test
    public void createComponentsWithoutSWT() {
        List<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();

        factories.add(new SarosEclipseContextFactory(saros));
        factories.add(new SarosCoreContextFactory());

        for (ISarosContextFactory factory : factories) {
            factory.createComponents(container);
        }

        Assert.assertNotNull(container.getComponents());
    }

    @Test
    public void createComponentsWithSWT() {
        List<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();

        factories.add(new SarosEclipseContextFactory(saros));
        factories.add(new SarosCoreContextFactory());
        factories.add(new HTMLUIContextFactory());
        factories.add(new EclipseHTMLUIContextFactory());

        for (ISarosContextFactory factory : factories) {
            factory.createComponents(container);
        }

        Assert.assertNotNull(container.getComponents());
    }
}
