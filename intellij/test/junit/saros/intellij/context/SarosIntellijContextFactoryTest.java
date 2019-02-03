package saros.intellij.context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.MockPolicy;
import saros.context.CoreContextFactory;
import saros.context.IContextFactory;
import saros.intellij.project.ProjectWrapper;
import saros.test.mocks.ContextMocker;
import saros.test.mocks.PrepareCoreComponents;

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
    IContextFactory factory = new SarosIntellijContextFactory(new ProjectWrapper(project));

    factory.createComponents(container);
    container.start();

    Assert.assertNotNull(container.getComponents());
  }
}
