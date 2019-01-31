package de.fu_berlin.inf.dpp.intellij.context;

import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareCoreComponents;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.MockPolicy;

/** Checks the {@link SarosIntellijContextFactory} for internal integrity. */
@MockPolicy(PrepareCoreComponents.class)
public class SarosIntellijContextFactoryTest extends AbstractContextTest {

  @Override
  @Before
  public void setup() {
    super.setup();

    // mock Saros/Core dependencies
    ContextMocker.addMocksFromFactory(container, new CoreContextFactory());
  }

  @Test
  public void testCreateComponents() {
    IContextFactory factory = new SarosIntellijContextFactory(project);

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
