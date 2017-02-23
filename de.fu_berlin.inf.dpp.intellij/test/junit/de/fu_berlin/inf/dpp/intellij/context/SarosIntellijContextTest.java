package de.fu_berlin.inf.dpp.intellij.context;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.context.CoreContextFactory;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static de.fu_berlin.inf.dpp.intellij.test.IntellijMocker.mockStaticGetInstance;

/**
 * Checks the Saros/I context for integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommandProcessor.class, FileDocumentManager.class,
    FileEditorManager.class, LocalFileSystem.class, PropertiesComponent.class })
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

        project = EasyMock.createNiceMock(Project.class);
        EasyMock.replay(project);

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

        Assert.assertNotNull(container.getComponents());
    }

}
