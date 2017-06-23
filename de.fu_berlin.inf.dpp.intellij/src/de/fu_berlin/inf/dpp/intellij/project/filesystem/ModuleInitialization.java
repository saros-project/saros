package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.picocontainer.Startable;

import java.io.File;
import java.io.IOException;

/**
 * ModuleInitialization ensures that all shared modules are registered in the project structure.
 */
public class ModuleInitialization implements Startable {

    private static final Logger LOG = Logger.getLogger(ModuleInitialization.class);

    private final ISarosSession session;
    private final Project project;

    private final ISessionListener moduleLoaderListener = new AbstractSessionListener() {

        /**
         * This method ensures that the transmitted content is shown as a module by loading the .iml file of the
         * transmitted module into the modules.xml file of the project.
         *
         * @param module the module to be added
         * @throws IllegalArgumentException if a module with the passed projectID could not be found
         */
        @Override
        public void resourcesAdded(IProject module) {
            final ModuleLoader moduleLoader = new ModuleLoader(module);

            //Registers a ModuleLoader with the AWT event dispatching thread to be executed asynchronously.
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(moduleLoader);
                }
            });
        }
    };

    public ModuleInitialization(ISarosSession session, Project project) {
        this.session = session;
        this.project = project;
    }

    @Override
    public void start() {
        session.addListener(moduleLoaderListener);
    }

    @Override
    public void stop() {
        session.removeListener(moduleLoaderListener);
    }

    /**
     * Runnable loading the .iml file of the added module using the method {@link ModuleManager#loadModule}.
     * This adds the module to the project structure.
     */
    private class ModuleLoader implements Runnable {
        private IProject module;

        public ModuleLoader(IProject module) {
            this.module = module;
        }

        @Override
        public void run() {
            final String moduleName = module.getName();
            final String filePath = module.getProjectRelativePath().toString() + File.separator + moduleName + ".iml";
            final ModuleManager moduleManager = ModuleManager.getInstance(ModuleInitialization.this.project);

            if (moduleManager.findModuleByName(moduleName) != null) {
                LOG.debug("ModuleInitialization aborted: module \"" + moduleName + "\" already present");
                return;
            }

            try {
                Module addedModule = moduleManager.loadModule(filePath);
                LOG.info("ModuleInitialization successful: module \"" + addedModule.getName()
                    + "\" added to local project structure");
            } catch (InvalidDataException | IOException | JDOMException e) {
                LOG.error("Failed to load module data for module " + moduleName + " from file " + filePath, e);
            } catch (ModuleWithNameAlreadyExists e) {
                LOG.error("Module already exists: ", e);
            }
        }
    }
}