package de.fu_berlin.inf.dpp.intellij.context;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareCoreComponents;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static de.fu_berlin.inf.dpp.intellij.test.IntellijMocker.mockStaticGetInstance;

/**
 * Checks the {@link SarosIntellijContextFactory} for internal integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommandProcessor.class, FileDocumentManager.class,
    FileEditorManager.class, LocalFileSystem.class, PropertiesComponent.class,
    ModuleTypeManager.class })
@MockPolicy(PrepareCoreComponents.class)
public class SarosIntellijContextFactoryTest {

    private MutablePicoContainer container;
    private Project project;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock Saros/Core dependencies
        ContextMocker
            .addMocksFromFactory(container, new CoreContextFactory());

        // mock IntelliJ dependencies
        mockStaticGetInstance(CommandProcessor.class, null);
        mockStaticGetInstance(FileDocumentManager.class, null);
        mockStaticGetInstance(FileEditorManager.class, Project.class);
        mockStaticGetInstance(LocalFileSystem.class, null);
        mockStaticGetInstance(PropertiesComponent.class, null);
        mockStaticGetInstance(ModuleTypeManager.class, null);

        project = EasyMock.createNiceMock(Project.class);
        EasyMock.replay(project);
    }

    @Test
    public void testCreateComponents() {
        IContextFactory factory = new SarosIntellijContextFactory(project);

        factory.createComponents(container);
        container.start();

        Assert.assertNotNull(container.getComponents());
    }
}
