package de.fu_berlin.inf.dpp.ui.browser;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;

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
