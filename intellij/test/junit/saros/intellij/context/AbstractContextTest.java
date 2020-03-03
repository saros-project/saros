package saros.intellij.context;

import static saros.intellij.test.IntellijMocker.mockStaticGetInstance;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.test.mocks.ContextMocker;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
  PropertiesComponent.class,
  ModuleTypeManager.class,
  IntelliJVersionProvider.class,
  ApplicationManager.class
})
public class AbstractContextTest {

  MutablePicoContainer container;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock IntelliJ dependencies
    mockStaticGetInstance(PropertiesComponent.class, null);
    mockStaticGetInstance(ModuleTypeManager.class, null);

    // mock IntelliJ dependent calls to get current IDE and plugin version
    PowerMock.mockStaticPartial(
        IntelliJVersionProvider.class, "getPluginVersion", "getBuildNumber");

    EasyMock.expect(IntelliJVersionProvider.getPluginVersion()).andReturn("0.1.0");
    EasyMock.expect(IntelliJVersionProvider.getBuildNumber()).andReturn("1");

    PowerMock.replay(IntelliJVersionProvider.class);

    // mock application related requests
    MessageBusConnection messageBusConnection = EasyMock.createNiceMock(MessageBusConnection.class);
    EasyMock.replay(messageBusConnection);

    MessageBus messageBus = EasyMock.createNiceMock(MessageBus.class);
    EasyMock.expect(messageBus.connect()).andReturn(messageBusConnection);
    EasyMock.replay(messageBus);

    Application application = EasyMock.createNiceMock(Application.class);
    EasyMock.expect(application.getMessageBus()).andReturn(messageBus);
    EasyMock.replay(application);

    PowerMock.mockStaticPartial(ApplicationManager.class, "getApplication");
    EasyMock.expect(ApplicationManager.getApplication()).andReturn(application);
    PowerMock.replay(ApplicationManager.class);
  }
}
