package saros.intellij.negotiation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

/** Data holder class for module configuration options used as part of the project negotiation. */
public class ModuleConfiguration {
  private final String sdkName;
  private final String moduleType;
  private final Map<JpsModuleSourceRootType<? extends JpsElement>, String[]> rootPaths;

  private final boolean existingModule;

  public ModuleConfiguration(@NotNull Map<String, String> options, boolean existingModule) {
    this.sdkName = options.get(ModuleConfigurationProvider.SDK_KEY);

    this.moduleType = options.get(ModuleConfigurationProvider.MODULE_TYPE_KEY);

    this.rootPaths = new HashMap<>();

    rootPaths.put(
        JavaSourceRootType.SOURCE,
        ModuleConfigurationProvider.split(
            options.get(ModuleConfigurationProvider.SOURCE_ROOTS_KEY)));

    rootPaths.put(
        JavaSourceRootType.TEST_SOURCE,
        ModuleConfigurationProvider.split(
            options.get(ModuleConfigurationProvider.TEST_SOURCE_ROOTS_KEY)));

    rootPaths.put(
        JavaResourceRootType.RESOURCE,
        ModuleConfigurationProvider.split(
            options.get(ModuleConfigurationProvider.RESOURCE_ROOTS_KEY)));

    rootPaths.put(
        JavaResourceRootType.TEST_RESOURCE,
        ModuleConfigurationProvider.split(
            options.get(ModuleConfigurationProvider.TEST_RESOURCE_ROOTS_KEY)));

    this.existingModule = existingModule;
  }

  /**
   * Returns the module type.
   *
   * @return the module type or <code>null</code> if no module type was received
   */
  @Nullable
  public String getModuleType() {
    return moduleType;
  }

  /**
   * Returns the module sdk name.
   *
   * @return the module sdk name or <code>null</code> if no module type was received
   */
  @Nullable
  public String getSdkName() {
    return sdkName;
  }

  /**
   * Returns the source root paths.
   *
   * @return the source root paths, values might be mapped to <code>null</code> if no paths were
   *     received for the root type
   */
  @NotNull
  public Map<JpsModuleSourceRootType<? extends JpsElement>, String[]> getRootPaths() {
    return Collections.unmodifiableMap(rootPaths);
  }

  /**
   * Returns whether the module was pre-existing.
   *
   * @return <code>true</code> if the module was pre-existing, <code>false</code> if it was created
   *     as part of the project negotiation
   */
  public boolean isExistingModule() {
    return existingModule;
  }
}
