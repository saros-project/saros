package saros.intellij.context;

import static saros.intellij.test.IntellijMocker.mockStaticGetInstance;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.project.Project;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.test.mocks.ContextMocker;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertiesComponent.class, ModuleTypeManager.class, IntelliJVersionProvider.class})
public class AbstractContextTest {

  MutablePicoContainer container;
  Project project;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock IntelliJ dependencies
    mockStaticGetInstance(PropertiesComponent.class, null);
    mockStaticGetInstance(ModuleTypeManager.class, null);

    project = EasyMock.createNiceMock(Project.class);

    EasyMock.replay(project);

    // mock IntelliJ dependent calls to get current IDE and plugin version
    PowerMock.mockStaticPartial(
        IntelliJVersionProvider.class, "getPluginVersion", "getBuildNumber");

    EasyMock.expect(IntelliJVersionProvider.getPluginVersion()).andReturn("0.1.0");
    EasyMock.expect(IntelliJVersionProvider.getBuildNumber()).andReturn("1");

    PowerMock.replay(IntelliJVersionProvider.class);
  }
}
