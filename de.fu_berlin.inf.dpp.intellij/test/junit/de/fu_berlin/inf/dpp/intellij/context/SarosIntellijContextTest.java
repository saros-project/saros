package de.fu_berlin.inf.dpp.intellij.context;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.intellij.test.IntellijMocker;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the Saros/I context for integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommandProcessor.class, FileDocumentManager.class,
    FileEditorManager.class, PropertiesComponent.class })
public class SarosIntellijContextTest {

    private MutablePicoContainer container;
    private Saros saros;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock IntelliJ dependencies
        IntellijMocker.mockStaticGetInstance(CommandProcessor.class, null);
        IntellijMocker.mockStaticGetInstance(FileDocumentManager.class, null);
        IntellijMocker
            .mockStaticGetInstance(FileEditorManager.class, Project.class);
        IntellijMocker.mockStaticGetInstance(PropertiesComponent.class, null);

        // mock Saros environment
        ContextMocker.addMock(container, ISarosContext.class);

        saros = IntellijMocker.mockSaros();
    }

    @Test
    public void createComponentsWithoutSWT() {
        List<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();

        factories.add(new SarosIntellijContextFactory(saros));
        factories.add(new SarosCoreContextFactory());

        for (ISarosContextFactory factory : factories) {
            factory.createComponents(container);
        }

        Assert.assertNotNull(container.getComponents());
    }

    @Test
    public void createComponentsWithSWT() {
        List<ISarosContextFactory> factories = new ArrayList<ISarosContextFactory>();

        factories.add(new SarosIntellijContextFactory(saros));
        factories.add(new SarosCoreContextFactory());
        factories.add(new HTMLUIContextFactory());

        for (ISarosContextFactory factory : factories) {
            factory.createComponents(container);
        }

        Assert.assertNotNull(container.getComponents());
    }

}
