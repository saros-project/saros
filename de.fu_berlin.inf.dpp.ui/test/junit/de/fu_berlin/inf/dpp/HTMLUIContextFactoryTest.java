package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;

import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRoot;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareCoreComponents;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.ide_embedding.IWebResourceLocator;
import de.fu_berlin.inf.dpp.ui.util.ICollaborationUtils;

/**
 * Checks the {@link HTMLUIContextFactory} for internal integrity.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy({ PrepareCoreComponents.class })
public class HTMLUIContextFactoryTest {

    private MutablePicoContainer container;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock Saros/Core components
        ContextMocker.addMocksFromFactory(container,
            new SarosCoreContextFactory());

        // mock dependencies normally provided by the IDE plugin
        Class<?>[] dependencies = { DialogManager.class,
            ICollaborationUtils.class, IWebResourceLocator.class,
            IWorkspaceRoot.class };

        ContextMocker.addMocks(container, Arrays.asList(dependencies));
    }

    @Test
    public void testCreateComponents() {
        ISarosContextFactory factory = new HTMLUIContextFactory();

        factory.createComponents(container);
        Assert.assertNotNull(container.getComponents());
    }
}
