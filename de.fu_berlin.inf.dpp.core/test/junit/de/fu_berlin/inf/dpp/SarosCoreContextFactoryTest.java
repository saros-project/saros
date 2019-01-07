package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;
import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.remote.IRemoteProgressIndicatorFactory;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.util.MemoryPreferenceStore;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;

/** Check the {@link CoreContextFactory} for internal integrity. */
public class SarosCoreContextFactoryTest {

  private MutablePicoContainer container;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock dependencies normally provided by the surrounding system (e.g.
    // an IDE plugin)
    Class<?>[] dependencies = {
      IProxyResolver.class,
      IRemoteProgressIndicatorFactory.class,
      IContainerContext.class,
      Preferences.class,
      IWorkspace.class,
      IEditorManager.class,
      IChecksumCache.class,
      IPathFactory.class
    };

    ContextMocker.addMocks(container, Arrays.asList(dependencies));

    // nice mocks aren't clever enough here
    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class), "1.0.0.dummy");

    // nice mocks aren't clever enough here
    container.addComponent(IPreferenceStore.class, new MemoryPreferenceStore());
  }

  @Test
  public void testCreateComponents() {
    IContextFactory factory = new CoreContextFactory();

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
