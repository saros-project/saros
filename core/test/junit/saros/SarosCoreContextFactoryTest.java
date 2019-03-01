package saros;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.BindKey;
import org.picocontainer.MutablePicoContainer;
import saros.communication.connection.IProxyResolver;
import saros.context.CoreContextFactory;
import saros.context.IContainerContext;
import saros.context.IContextFactory;
import saros.context.IContextKeyBindings;
import saros.editor.IEditorManager;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IWorkspace;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.test.mocks.ContextMocker;
import saros.test.util.MemoryPreferenceStore;

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
      IChecksumCache.class
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
