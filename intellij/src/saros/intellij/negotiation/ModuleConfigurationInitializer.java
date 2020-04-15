package saros.intellij.negotiation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import saros.filesystem.IProject;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.SafeDialogUtils;
import saros.repackaged.picocontainer.Startable;
import saros.session.ISarosSession;
import saros.session.ISessionListener;

/**
 * This class ensures that modules which were transferred as part of the project negotiation are
 * correctly configured. This is done by loading the correct module configuration from the
 * additional project negotiation data once the module is added to the session.
 */
public class ModuleConfigurationInitializer implements Startable {
  private static final Logger log = Logger.getLogger(ModuleConfigurationInitializer.class);

  private final ISarosSession session;

  private final Map<Module, ModuleConfiguration> queuedModuleOptions;

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void resourcesAdded(IProject project) {
          applyModuleConfiguration(project);
        }
      };

  public ModuleConfigurationInitializer(ISarosSession session) {
    this.session = session;

    this.queuedModuleOptions = new ConcurrentHashMap<>();
  }

  @Override
  public void start() {
    session.addListener(sessionListener);
  }

  @Override
  public void stop() {
    session.removeListener(sessionListener);
  }

  /**
   * Enqueues module options for a module created as part of the project negotiation. This causes
   * the passed options to be applied to the module once it is officially added to the session.
   *
   * @param module the module to apply the options to
   * @param moduleConfiguration the configuration options to apply to the module
   */
  public void enqueueModuleConfigurationChange(
      @NotNull Module module, @NotNull ModuleConfiguration moduleConfiguration) {

    log.debug(
        "Queued module config for module "
            + module
            + " - existing? "
            + moduleConfiguration.isExistingModule());

    queuedModuleOptions.put(module, moduleConfiguration);
  }

  /**
   * Updates the shared module with the queued module options. Does nothing if no such options are
   * found in the held queue.
   *
   * @param wrappedModule the <code>IProject</code> representing the shared module
   */
  private void applyModuleConfiguration(@NotNull IProject wrappedModule) {
    Module module = ((IntelliJProjectImpl) wrappedModule).getModule();

    ModuleConfiguration moduleConfiguration = queuedModuleOptions.remove(module);

    if (moduleConfiguration == null) {
      log.debug("Skipped module root model adjustment as not options were found - " + module);

      return;
    }

    if (!moduleConfiguration.isExistingModule()) {
      ModuleRootModificationUtil.updateModel(
          module,
          modifiableRootModel ->
              applyModuleConfiguration(modifiableRootModel, moduleConfiguration));

      return;
    }

    ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();

    if (contentEntries.length != 1) {
      log.error("Encountered shared module with multiple content roots - " + module);

      return;
    }

    ContentEntry contentEntry = contentEntries[0];

    if (!configurationDiffers(contentEntry, moduleConfiguration.getRootPaths())) {
      return;
    }

    Runnable changeModuleConfiguration =
        () ->
            ModuleRootModificationUtil.updateModel(
                module,
                modifiableRootModel -> {
                  resetModuleConfiguration(modifiableRootModel);
                  applyModuleConfiguration(modifiableRootModel, moduleConfiguration);
                });

    SafeDialogUtils.showYesNoDialog(
        module.getProject(),
        Messages.ModuleConfigurationInitializer_override_module_config_message,
        Messages.ModuleConfigurationInitializer_override_module_config_title,
        changeModuleConfiguration);
  }

  /**
   * Returns whether the content entry source root configuration matches the passed configuration.
   *
   * @param contentEntry the content entry to check
   * @param rootPaths the root paths received from the host
   * @return <code>true</code> if the root paths of the passed content entry match the paths
   *     received from the host, <code>false</code> if the configuration matches or the
   *     configuration could not be read
   */
  private boolean configurationDiffers(
      @NotNull ContentEntry contentEntry,
      @NotNull Map<JpsModuleSourceRootType<? extends JpsElement>, String[]> rootPaths) {

    VirtualFile contentEntryFile = contentEntry.getFile();

    if (contentEntryFile == null) {
      log.error(
          "Encountered content root without a valid local representation for shared module \""
              + contentEntry.getRootModel().getModule()
              + "\". Can not provide source configuration.");

      return false;
    }

    Path basePath = Paths.get(contentEntryFile.getPath());

    for (Entry<JpsModuleSourceRootType<? extends JpsElement>, String[]> entry :
        rootPaths.entrySet()) {

      JpsModuleSourceRootType<? extends JpsElement> type = entry.getKey();
      String[] paths = entry.getValue();

      Set<String> remoteRoots;
      if (paths != null) {
        remoteRoots = Arrays.stream(paths).collect(Collectors.toSet());
      } else {
        remoteRoots = Collections.emptySet();
      }

      Set<String> localRoots =
          contentEntry
              .getSourceFolders(type)
              .stream()
              .map(sourceFolder -> ModuleUtils.getRelativeRootPath(basePath, sourceFolder))
              .filter(Objects::nonNull)
              .map(Path::toString)
              .collect(Collectors.toSet());

      if (!remoteRoots.equals(localRoots)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes all source roots from he given root model.
   *
   * @param modifiableRootModel the root model to modify
   */
  private void resetModuleConfiguration(@NotNull ModifiableRootModel modifiableRootModel) {
    log.debug("Resetting module configuration for module " + modifiableRootModel.getModule());

    Module module = modifiableRootModel.getModule();

    ContentEntry[] contentEntries = modifiableRootModel.getContentEntries();

    if (contentEntries.length != 1) {
      log.error("Encountered shared module with multiple content roots - " + module);

      return;
    }

    ContentEntry contentEntry = contentEntries[0];

    VirtualFile contentEntryFile = contentEntry.getFile();

    if (contentEntryFile == null) {
      log.error(
          "Content root for shared module does not have a valid local representation - " + module);

      return;
    }

    Arrays.stream(contentEntry.getSourceFolders()).forEach(contentEntry::removeSourceFolder);
  }

  /**
   * Applies the additional module options shared as part of the module project negotiation data to
   * the given modifiable module root model.
   *
   * <p>The available options are defined through keys in {@link ModuleConfigurationProvider}.
   *
   * @param modifiableRootModel the modifiable root model of the module
   * @param moduleConfiguration the configuration options for the module
   */
  private void applyModuleConfiguration(
      @NotNull ModifiableRootModel modifiableRootModel,
      @NotNull ModuleConfiguration moduleConfiguration) {

    log.debug("Applying module configuration for module " + modifiableRootModel.getModule());

    Module module = modifiableRootModel.getModule();

    String sdkName = moduleConfiguration.getSdkName();

    if (sdkName != null) {
      Sdk sdk;
      if (sdkName.isEmpty() || (sdk = ProjectJdkTable.getInstance().findJdk(sdkName)) == null) {
        modifiableRootModel.inheritSdk();

      } else {
        modifiableRootModel.setSdk(sdk);
      }
    } else {
      log.warn(
          "Did not receive any SDK configuration data. This should not be possible and could be an indication for a version mismatch.");
    }

    ContentEntry[] contentEntries = modifiableRootModel.getContentEntries();

    if (contentEntries.length != 1) {
      log.error("Encountered shared module with multiple content roots - " + module);

      return;
    }

    ContentEntry contentEntry = contentEntries[0];

    VirtualFile contentEntryFile = contentEntry.getFile();

    if (contentEntryFile == null) {
      log.error(
          "Content root for shared module does not have a valid local representation - " + module);

      return;
    }

    moduleConfiguration
        .getRootPaths()
        .forEach((type, paths) -> addRoot(contentEntry, contentEntryFile, type, paths));
  }

  /**
   * Adds a source root of the given type to the given content entry for every path contained in the
   * given relative source root paths. Source roots whose relative path could not be successfully
   * resolved against the content root of the module are ignored.
   *
   * <p>Does nothing if the string representing the relative source root paths is <code>null
   * </code>.
   *
   * @param contentEntry the content entry to add the roots to
   * @param contentEntryFile the virtual file representing the content root
   * @param rootType the type of source root to add the paths as
   * @param relativeSourceRootPaths the list of relative path to add as source roots
   * @param <T> the type of the source root
   */
  private <T extends JpsElement> void addRoot(
      @NotNull ContentEntry contentEntry,
      @NotNull VirtualFile contentEntryFile,
      @NotNull JpsModuleSourceRootType<T> rootType,
      @Nullable String[] relativeSourceRootPaths) {

    if (relativeSourceRootPaths == null) {
      return;
    }

    for (String relativeSourcePath : relativeSourceRootPaths) {
      VirtualFile sourceRoot = contentEntryFile.findFileByRelativePath(relativeSourcePath);

      if (sourceRoot != null) {
        contentEntry.addSourceFolder(sourceRoot, rootType);

      } else {
        log.debug(
            "Skipping source root addition for module "
                + contentEntry.getRootModel().getModule()
                + " as path resolution failed - type: "
                + rootType
                + ", relative path: "
                + relativeSourcePath);
      }
    }
  }
}
