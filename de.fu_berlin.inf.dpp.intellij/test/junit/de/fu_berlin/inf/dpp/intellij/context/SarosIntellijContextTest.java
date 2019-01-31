package de.fu_berlin.inf.dpp.intellij.context;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Checks the Saros/I context for integrity. */
public class SarosIntellijContextTest extends AbstractContextTest {

  @Override
  @Before
  public void setup() {
    super.setup();

    // mock Saros environment
    ContextMocker.addMock(container, IContainerContext.class);
  }

  @Test
  public void createComponentsWithoutSWT() {
    List<IContextFactory> factories = new ArrayList<>();

    factories.add(new SarosIntellijContextFactory(project));
    factories.add(new CoreContextFactory());

    for (IContextFactory factory : factories) {
      factory.createComponents(container);
    }

    container.start();

    Assert.assertNotNull(container.getComponents());
  }

  @Test
  public void createComponentsWithSWT() {
    List<IContextFactory> factories = new ArrayList<>();

    factories.add(new SarosIntellijContextFactory(project));
    factories.add(new CoreContextFactory());
    factories.add(new HTMLUIContextFactory());

    for (IContextFactory factory : factories) {
      factory.createComponents(container);
    }

    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
