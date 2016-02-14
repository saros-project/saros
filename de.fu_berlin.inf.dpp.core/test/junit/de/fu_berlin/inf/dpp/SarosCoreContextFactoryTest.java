package de.fu_berlin.inf.dpp;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;

/**
 * Check the {@link SarosCoreContextFactory} for internal integrity.
 */
public class SarosCoreContextFactoryTest {

    private MutablePicoContainer container;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock dependencies normally provided by the surrounding system (e.g.
        // an IDE plugin)
        Class<?>[] dependencies = { IProxyResolver.class,
            IRemoteProgressIndicatorFactory.class, ISarosContext.class,
            Preferences.class };

        ContextMocker.addMocks(container, Arrays.asList(dependencies));

        // nice mocks aren't clever enough here
        container.addComponent(BindKey.bindKey(String.class,
            ISarosContextBindings.SarosVersion.class), "1.0.0.dummy");

        // nice mocks aren't clever enough here
        container.addComponent(IPreferenceStore.class,
            new MemoryPreferenceStore());
    }

    @Test
    public void testCreateComponents() {
        ISarosContextFactory factory = new SarosCoreContextFactory();
        factory.createComponents(container);

        Assert.assertNotNull(container.getComponents());
    }
}
