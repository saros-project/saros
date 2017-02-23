package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.project.Project;

import de.fu_berlin.inf.dpp.AbstractSarosLifecycle;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.intellij.context.SarosIntellijContextFactory;
import de.fu_berlin.inf.dpp.intellij.ui.swt_browser.SwtLibLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extends the {@link AbstractSarosLifecycle} for an IntelliJ plug-in.
 * It contains additional IntelliJ specific fields and methods.
 * <p/>
 * <p/>
 * This class is a singleton.
 */
public class IntellijSarosLifecycle extends AbstractSarosLifecycle {

    private static IntellijSarosLifecycle instance;

    /**
     * Creates a new IntelliJSarosLifecycle singleton instance from a project.
     *
     * @param project
     * @return
     */
    public static synchronized IntellijSarosLifecycle getInstance(
        Project project) {
        instance = new IntellijSarosLifecycle(project);

        return instance;
    }

    private Project project;

    private IntellijSarosLifecycle(Project project) {
        this.project = project;
    }

    @Override
    protected Collection<IContextFactory> additionalContextFactories() {
        List<IContextFactory> nonCoreFactories = new ArrayList<IContextFactory>();

        nonCoreFactories.add(new SarosIntellijContextFactory(project));

        if (SarosComponent.isSwtBrowserEnabled()) {
            SwtLibLoader.loadSwtLib();
            nonCoreFactories.add(new HTMLUIContextFactory());
        }

        return nonCoreFactories;
    }
}
