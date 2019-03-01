package saros;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.context.CoreContextFactory;
import saros.context.IContextFactory;
import saros.filesystem.IWorkspaceRoot;
import saros.test.mocks.ContextMocker;
import saros.test.mocks.PrepareCoreComponents;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.ide_embedding.IUIResourceLocator;
import saros.ui.util.ICollaborationUtils;

/** Checks the {@link HTMLUIContextFactory} for internal integrity. */
@RunWith(PowerMockRunner.class)
@MockPolicy({PrepareCoreComponents.class})
public class HTMLUIContextFactoryTest {

  private MutablePicoContainer container;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock Saros/Core components
    ContextMocker.addMocksFromFactory(container, new CoreContextFactory());

    // mock dependencies normally provided by the IDE plugin
    Class<?>[] dependencies = {
      DialogManager.class, ICollaborationUtils.class, IUIResourceLocator.class, IWorkspaceRoot.class
    };

    ContextMocker.addMocks(container, Arrays.asList(dependencies));
  }

  @Test
  public void testCreateComponents() {
    IContextFactory factory = new HTMLUIContextFactory();

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
