package de.fu_berlin.inf.dpp.intellij.context;

import static de.fu_berlin.inf.dpp.intellij.test.IntellijMocker.mockStaticGetInstance;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** Checks the Saros/I context for integrity. */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
  CommandProcessor.class,
  FileDocumentManager.class,
  FileEditorManager.class,
  LocalFileSystem.class,
  PropertiesComponent.class,
  ModuleTypeManager.class,
  IntelliJVersionProvider.class
})
public class SarosIntellijContextTest {

  private MutablePicoContainer container;
  private Project project;

  @Before
  public void setup() {
    container = ContextMocker.emptyContext();

    // mock IntelliJ dependencies
    mockStaticGetInstance(CommandProcessor.class, null);
    mockStaticGetInstance(FileDocumentManager.class, null);
    mockStaticGetInstance(FileEditorManager.class, Project.class);
    mockStaticGetInstance(LocalFileSystem.class, null);
    mockStaticGetInstance(PropertiesComponent.class, null);
    mockStaticGetInstance(ModuleTypeManager.class, null);

    // mock IntelliJ message bus used to listen for editor activities
    MessageBusConnection messageBusConnection = EasyMock.createNiceMock(MessageBusConnection.class);

    EasyMock.replay(messageBusConnection);

    MessageBus messageBus = EasyMock.createNiceMock(MessageBus.class);
    EasyMock.expect(messageBus.connect()).andReturn(messageBusConnection);

    EasyMock.replay(messageBus);

    project = EasyMock.createNiceMock(Project.class);
    EasyMock.expect(project.getMessageBus()).andReturn(messageBus);

    EasyMock.replay(project);

    // mock IntelliJ dependent calls to get current IDE and plugin version
    PowerMock.mockStaticPartial(
        IntelliJVersionProvider.class, "getPluginVersion", "getBuildNumber");

    EasyMock.expect(IntelliJVersionProvider.getPluginVersion()).andReturn("0.1.0");
    EasyMock.expect(IntelliJVersionProvider.getBuildNumber()).andReturn("1");

    PowerMock.replay(IntelliJVersionProvider.class);

    // mock Saros environment
    ContextMocker.addMock(container, IContainerContext.class);
  }

  @Test
  public void createComponentsWithoutSWT() {
    List<IContextFactory> factories = new ArrayList<IContextFactory>();

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
    List<IContextFactory> factories = new ArrayList<IContextFactory>();

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
