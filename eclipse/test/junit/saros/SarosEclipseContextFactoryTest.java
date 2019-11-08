package saros;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.context.CoreContextFactory;
import saros.context.IContextFactory;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.test.mocks.ContextMocker;
import saros.test.mocks.EclipseMocker;
import saros.test.mocks.PrepareCoreComponents;
import saros.test.mocks.PrepareEclipseComponents;

/** Check {@link SarosEclipseContextFactory} for internal integrity. */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Saros.class})
@MockPolicy({PrepareCoreComponents.class, PrepareEclipseComponents.class})
@PowerMockIgnore({"javax.xml.*"})
public class SarosEclipseContextFactoryTest {

  private MutablePicoContainer container;
  private Saros saros;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock Saros/Core components
    ContextMocker.addMocksFromFactory(container, new CoreContextFactory());

    // mock Saros environment
    saros = EclipseMocker.mockSaros();

    // mock Eclipse dependencies
    EclipseMocker.mockResourcesPlugin();
    EclipseMocker.mockPlatform();
    EclipseMocker.mockSWTDisplay();
  }

  @Test
  public void testCreateComponents() {
    IContextFactory factory = new SarosEclipseContextFactory(saros);

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
