package saros.intellij.negotiation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointManager;
import saros.intellij.filesystem.IntelliJProjectImpl;
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

  private final Map<Module, Map<String, String>> queuedModuleOptions;

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void resourcesAdded(IReferencePoint referencePoint) {
          applyModuleConfiguration(referencePoint);
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
   * @param configurationOptions the configuration options to apply to the module
   */
  public void enqueueModuleConfigurationChange(
      @NotNull Module module, @NotNull Map<String, String> configurationOptions) {

    queuedModuleOptions.put(module, configurationOptions);
  }

  /**
   * Updates the shared module with the queued module options. Does nothing if no such options are
   * found in the held queue.
   *
   * @param referencePoint the <code>IReferencePoint</code> representing the shared module
   */
  private void applyModuleConfiguration(@NotNull IReferencePoint referencePoint) {
    IReferencePointManager referencePointManger =
        session.getComponent(IReferencePointManager.class);
    Module module =
        referencePointManger
            .getProject(referencePoint)
            .adaptTo(IntelliJProjectImpl.class)
            .getModule();

    Map<String, String> configurationOptions = queuedModuleOptions.remove(module);

    if (configurationOptions == null) {
      log.debug("Skipped module root model adjustment as not options were found - " + module);

      return;
    }

    ModuleRootModificationUtil.updateModel(
        module,
        modifiableRootModel -> applyModuleConfiguration(modifiableRootModel, configurationOptions));
  }

  /**
   * Applies the additional module options shared as part of the module project negotiation data to
   * the given modifiable module root model.
   *
   * <p>The available options are defined through keys in {@link ModuleConfigurationProvider}.
   *
   * @param modifiableRootModel the modifiable root model of the module
   * @param configurationOptions the configuration options for the module
   */
  private void applyModuleConfiguration(
      @NotNull ModifiableRootModel modifiableRootModel,
      @NotNull Map<String, String> configurationOptions) {

    Module module = modifiableRootModel.getModule();

    ContentEntry[] contentEntries = modifiableRootModel.getContentEntries();

    if (contentEntries.length != 1) {
      log.error("Encountered shared module with multiple content roots - " + module);
    }

    ContentEntry contentEntry = contentEntries[0];

    String sdkName = configurationOptions.get(ModuleConfigurationProvider.SDK_KEY);

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

    VirtualFile contentRoot = contentEntry.getFile();

    if (contentRoot == null) {
      log.error(
          "Content root for shared module does not have a valid local representation - " + module);

      return;
    }

    addRoot(
        module,
        contentEntry,
        contentRoot,
        JavaSourceRootType.SOURCE,
        configurationOptions.get(ModuleConfigurationProvider.SOURCE_ROOTS_KEY));

    addRoot(
        module,
        contentEntry,
        contentRoot,
        JavaSourceRootType.TEST_SOURCE,
        configurationOptions.get(ModuleConfigurationProvider.TEST_SOURCE_ROOTS_KEY));

    addRoot(
        module,
        contentEntry,
        contentRoot,
        JavaResourceRootType.RESOURCE,
        configurationOptions.get(ModuleConfigurationProvider.RESOURCE_ROOTS_KEY));

    addRoot(
        module,
        contentEntry,
        contentRoot,
        JavaResourceRootType.TEST_RESOURCE,
        configurationOptions.get(ModuleConfigurationProvider.TEST_RESOURCE_ROOTS_KEY));
  }

  /**
   * Adds a source root of the given type to the given content entry for every path contained in the
   * given relative source root paths. Source roots whose relative path could not be successfully
   * resolved against the content root of the module are ignored.
   *
   * <p>Does nothing if the string representing the relative source root paths is <code>null
   * </code>.
   *
   * @param module the module to add source root entry for
   * @param contentEntry the content entry to add the roots to
   * @param contentEntryFile the virtual file representing the content root
   * @param rootType the type of source root to add the paths as
   * @param relativeSourceRootPaths the list of relative path to add as source roots
   * @param <T> the type of the source root
   */
  private <T extends JpsElement> void addRoot(
      @NotNull Module module,
      @NotNull ContentEntry contentEntry,
      @NotNull VirtualFile contentEntryFile,
      @NotNull JpsModuleSourceRootType<T> rootType,
      @Nullable String relativeSourceRootPaths) {

    if (relativeSourceRootPaths == null) {
      return;
    }

    String[] relativeSourcePaths = ModuleConfigurationProvider.split(relativeSourceRootPaths);

    for (String relativeSourcePath : relativeSourcePaths) {
      VirtualFile sourceRoot = contentEntryFile.findFileByRelativePath(relativeSourcePath);

      if (sourceRoot != null) {
        contentEntry.addSourceFolder(sourceRoot, rootType);

      } else {
        /*
         * TODO drop module parameter and use content entry module root model instead when
         *  backwards compatibility is dropped to 2019.2 or later
         */
        log.debug(
            "Skipping source root addition for module "
                + module
                + " as path resolution failed - type: "
                + rootType
                + ", relative path: "
                + relativeSourcePath);
      }
    }
  }
}
