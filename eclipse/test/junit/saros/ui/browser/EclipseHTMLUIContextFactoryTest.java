package saros.ui.browser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import saros.HTMLUIContextFactory;
import saros.context.IContextFactory;
import saros.synchronize.UISynchronizer;
import saros.test.mocks.ContextMocker;

/** Check {@link EclipseHTMLUIContextFactory} for internal integrity. */
public class EclipseHTMLUIContextFactoryTest {

  private MutablePicoContainer container;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock dependencies of HTMLUIContextFactory
    ContextMocker.addMocksFromFactory(container, new HTMLUIContextFactory());

    // mock dependencies normally provided from an IDE plugin
    ContextMocker.addMock(container, UISynchronizer.class);
  }

  @Test
  public void testCreateComponents() {
    IContextFactory factory = new EclipseHTMLUIContextFactory();

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
