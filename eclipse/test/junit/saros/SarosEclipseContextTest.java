package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.EclipseMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareEclipseComponents;
import de.fu_berlin.inf.dpp.ui.browser.EclipseHTMLUIContextFactory;
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

/** Checks the Saros/E context for integrity. */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Saros.class})
@MockPolicy({PrepareEclipseComponents.class})
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

    ContextMocker.addMock(container, IContainerContext.class);
  }

  @Test
  public void createComponentsWithoutSWT() {
    List<IContextFactory> factories = new ArrayList<IContextFactory>();

    factories.add(new SarosEclipseContextFactory(saros));
    factories.add(new CoreContextFactory());

    for (IContextFactory factory : factories) {
      factory.createComponents(container);
    }

    container.start();

    Assert.assertNotNull(container.getComponents());
  }

  @Test
  public void createComponentsWithSWT() {
    List<IContextFactory> factories = new ArrayList<IContextFactory>();

    factories.add(new SarosEclipseContextFactory(saros));
    factories.add(new CoreContextFactory());
    factories.add(new HTMLUIContextFactory());
    factories.add(new EclipseHTMLUIContextFactory());

    for (IContextFactory factory : factories) {
      factory.createComponents(container);
    }

    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
