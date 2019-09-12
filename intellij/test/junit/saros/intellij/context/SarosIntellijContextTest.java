package saros.intellij.context;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import saros.context.CoreContextFactory;
import saros.context.IContainerContext;
import saros.context.IContextFactory;
import saros.test.mocks.ContextMocker;

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

    factories.add(new SarosIntellijContextFactory());
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

    factories.add(new SarosIntellijContextFactory());
    factories.add(new CoreContextFactory());

    for (IContextFactory factory : factories) {
      factory.createComponents(container);
    }

    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
