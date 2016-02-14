package de.fu_berlin.inf.dpp.intellij.context;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.ISarosContextFactory;
import de.fu_berlin.inf.dpp.SarosCoreContextFactory;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.intellij.test.IntellijMocker;
import de.fu_berlin.inf.dpp.test.mocks.ContextMocker;
import de.fu_berlin.inf.dpp.test.mocks.PrepareCoreComponents;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.MutablePicoContainer;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Checks the {@link SarosIntellijContextFactory} for internal integrity.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommandProcessor.class, FileDocumentManager.class,
    FileEditorManager.class, PropertiesComponent.class })
@MockPolicy(PrepareCoreComponents.class)
public class SarosIntellijContextFactoryTest {

    private MutablePicoContainer container;
    private Saros saros;

    @Before
    public void setup() {
        container = ContextMocker.emptyContext();

        // mock Saros/Core dependencies
        ContextMocker
            .addMocksFromFactory(container, new SarosCoreContextFactory());

        // mock Saros environment
        saros = IntellijMocker.mockSaros();

        // mock IntelliJ dependencies
        IntellijMocker.mockStaticGetInstance(CommandProcessor.class, null);
        IntellijMocker.mockStaticGetInstance(FileDocumentManager.class, null);
        IntellijMocker
            .mockStaticGetInstance(FileEditorManager.class, Project.class);
        IntellijMocker.mockStaticGetInstance(PropertiesComponent.class, null);
    }

    @Test
    public void testCreateComponents() {
        ISarosContextFactory factory = new SarosIntellijContextFactory(saros);

        factory.createComponents(container);
        Assert.assertNotNull(container.getComponents());
    }
}