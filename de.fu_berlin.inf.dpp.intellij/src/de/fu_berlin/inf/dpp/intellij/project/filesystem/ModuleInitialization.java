package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.exceptions.ModuleNotFoundException;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * This class is instantiated by the PicoContainer as part of the session context.
 *
 * <p>It ensures that modules which were transferred as part of the project negotiation are
 * correctly registered with the local IntelliJ instance by reloading them with the transferred
 * module file.
 *
 * <p>This reload is needed as transferred modules are first created as an empty module stub during
 * the project negotiation. This creates an initial module file which is overwritten by the real
 * module file of the transferred module. As IntelliJ can not handle external change to module files
 * without reloading the entire project (which would reset the current session), the module is
 * disposed and then reloaded instead.
 *
 * @see ModuleReloader
 * @see AddProjectToSessionWizard#createModuleStub(String)
 */
public class ModuleInitialization implements Startable {

  private static final Logger LOG = Logger.getLogger(ModuleInitialization.class);

  private final ISarosSession session;

  private final ISessionListener moduleReloaderListener =
      new ISessionListener() {

        @Override
        public void resourcesAdded(IProject module) {
          final ModuleReloader moduleReloader = new ModuleReloader(module);

          // Registers a ModuleLoader with the AWT event dispatching thread to be executed
          // asynchronously.
          ApplicationManager.getApplication()
              .invokeLater(
                  new Runnable() {
                    @Override
                    public void run() {
                      ApplicationManager.getApplication().runWriteAction(moduleReloader);
                    }
                  });
        }
      };

  public ModuleInitialization(ISarosSession session) {
    this.session = session;
  }

  @Override
  public void start() {
    session.addListener(moduleReloaderListener);
  }

  @Override
  public void stop() {
    session.removeListener(moduleReloaderListener);
  }

  /**
   * Runnable disposing and then reloading the added module using the transferred module file. To
   * ensure that the transferred module file is used, the <code>VirtualFile</code> representing it
   * is refreshed synchronously.
   *
   * <p>After the module is reloaded, {@link IntelliJProjectImpl#refreshModule()} is called to
   * replace the old <code>Module</code> object held by the given <code>IProject</code>. This is
   * needed as calls on disposed modules result in an exception and the <code>IProject</code> object
   * is at this point already held by other classes.
   *
   * @see IntelliJProjectImpl#refreshModule()
   */
  private class ModuleReloader implements Runnable {
    private IntelliJProjectImpl project;

    public ModuleReloader(IProject project) {
      this.project = project.adaptTo(IntelliJProjectImpl.class);
    }

    @Override
    public void run() {
      Module module = project.getModule();

      if (IntelliJProjectImpl.RELOAD_STUB_MODULE_TYPE.equals(ModuleType.get(module).getId())) {

        String moduleName = module.getName();
        String moduleFilePath = module.getModuleFilePath();

        VirtualFile moduleFile = module.getModuleFile();

        if (moduleFile == null) {
          LOG.error(
              "Failed to load module data for module "
                  + moduleName
                  + " from file "
                  + moduleFilePath);

          return;
        }

        moduleFile.refresh(false, false);

        ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());

        ModifiableModuleModel modifiableModuleModel = moduleManager.getModifiableModel();

        modifiableModuleModel.disposeModule(module);

        try {
          modifiableModuleModel.loadModule(moduleFilePath);

          LOG.info(
              "ModuleInitialization successful: module \""
                  + moduleName
                  + "\" added to local project structure");

        } catch (InvalidDataException | IOException e) {
          LOG.error(
              "Failed to load module data for module "
                  + moduleName
                  + " from file "
                  + moduleFilePath,
              e);

        } catch (ModuleWithNameAlreadyExists e) {
          LOG.error("Module already exists: ", e);
        }

        modifiableModuleModel.commit();

        try {
          if (!project.refreshModule()) {
            LOG.error(
                "Failed to refresh the module object for " + project + " as it it not disposed.");
          }
        } catch (ModuleNotFoundException | IllegalArgumentException | IllegalStateException e) {
          LOG.error("Failed to refresh the module object for " + project, e);
        }
        // TODO clean up excluded module roots
      }
    }
  }
}
