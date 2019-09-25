package saros.intellij.negotiation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import saros.filesystem.IProject;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.negotiation.AdditionalProjectDataFactory;
import saros.negotiation.AdditionalProjectDataFactory.ProjectDataProvider;
import saros.negotiation.ProjectNegotiationData;

/**
 * Class providing additional module data needed to create a local representation of the shared
 * module.
 */
public class ModuleConfigurationProvider implements ProjectDataProvider {
  private static Logger log = Logger.getLogger(ModuleConfigurationProvider.class);

  static final String MODULE_TYPE_KEY = "MODULE_TYPE";
  static final String SDK_KEY = "SKD";
  static final String SOURCE_ROOTS_KEY = "SOURCE_ROOTS";
  static final String TEST_SOURCE_ROOTS_KEY = "TEST_SOURCE_ROOTS";
  static final String RESOURCE_ROOTS_KEY = "RESOURCE_ROOTS";
  static final String TEST_RESOURCE_ROOTS_KEY = "TEST_RESOURCE_ROOTS";

  private static final CharSequence DELIMITER = ":";

  private static final CharSequence ESCAPE_CHARACTER = "\\";

  private static final CharSequence ESCAPE_SEQUENCE =
      ESCAPE_CHARACTER.toString() + DELIMITER.toString();

  private static final Pattern SPLIT_PATTERN =
      Pattern.compile(
          "(?<!"
              + Pattern.quote(ESCAPE_CHARACTER.toString())
              + ")"
              + Pattern.quote(DELIMITER.toString()));

  /**
   * Instantiates a new module options data provider.
   *
   * <p><b>NOTE:</b> This class is meant to be singleton. This constructor should only be used by
   * the PicoContainer and must not be called directly.
   */
  public ModuleConfigurationProvider(AdditionalProjectDataFactory additionalProjectDataFactory) {
    additionalProjectDataFactory.registerProjectDataProvider(this);
  }

  // TODO consider also including module dependencies
  // TODO adjust once multiple content roots are possible
  @Override
  @NotNull
  public Map<String, String> getMapping(@NotNull IProject project) {
    Map<String, String> optionsMap = new HashMap<>();

    Module module = project.adaptTo(IntelliJProjectImpl.class).getModule();

    String moduleTypeName = module.getModuleTypeName();

    optionsMap.put(MODULE_TYPE_KEY, moduleTypeName);

    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

    Sdk sdk = moduleRootManager.getSdk();

    String sdkName;
    if (sdk != null) {
      sdkName = sdk.getName();
    } else {
      sdkName = "";
    }
    optionsMap.put(SDK_KEY, sdkName);

    ContentEntry[] contentEntries = moduleRootManager.getContentEntries();

    if (contentEntries.length != 1) {
      log.error(
          "Encountered shared module \""
              + module
              + "\" with multiple content roots. Can not provide source configuration.");

      return optionsMap;
    }

    ContentEntry contentEntry = contentEntries[0];

    VirtualFile contentRoot = contentEntry.getFile();

    if (contentRoot == null) {
      log.error(
          "Encountered content root without a valid local representation for shared module \""
              + module
              + "\". Can not provide source configuration.");

      return optionsMap;
    }

    Path contentRootPath = Paths.get(contentRoot.getPath());

    String sourceRoots =
        flatten(contentRootPath, contentEntry.getSourceFolders(JavaSourceRootType.SOURCE));
    optionsMap.put(SOURCE_ROOTS_KEY, sourceRoots);

    String testSourceRoots =
        flatten(contentRootPath, contentEntry.getSourceFolders(JavaSourceRootType.TEST_SOURCE));
    optionsMap.put(TEST_SOURCE_ROOTS_KEY, testSourceRoots);

    String resourceRoots =
        flatten(contentRootPath, contentEntry.getSourceFolders(JavaResourceRootType.RESOURCE));
    optionsMap.put(RESOURCE_ROOTS_KEY, resourceRoots);

    String testResourceRoots =
        flatten(contentRootPath, contentEntry.getSourceFolders(JavaResourceRootType.TEST_RESOURCE));
    optionsMap.put(TEST_RESOURCE_ROOTS_KEY, testResourceRoots);

    return optionsMap;
  }

  /**
   * Flattens the list of source folders into a single string containing their relative paths.
   *
   * <p>This is done by first making the paths relative to the passed base path, escaping any
   * existing usages of {@link #DELIMITER}, and then joining them into a single string separated by
   * {@link #DELIMITER}.
   *
   * @param basePath the base path to relativize against
   * @param sourceFolders the list of source folders to relativize and flatten
   * @return a string containing the relative paths of the passed source folder or <code>null</code>
   *     of the passed list of source folders is empty
   */
  @Nullable
  private String flatten(@NotNull Path basePath, @NotNull List<SourceFolder> sourceFolders) {
    if (sourceFolders.isEmpty()) {
      return null;
    }

    return sourceFolders
        .stream()
        .map(sourceFolder -> ModuleUtils.getRelativeRootPath(basePath, sourceFolder))
        .filter(Objects::nonNull)
        .map(Path::toString)
        .map(ModuleConfigurationProvider::escape)
        .collect(Collectors.joining(DELIMITER));
  }

  /**
   * Returns a list of all options contained in the passed string.
   *
   * <p>This is a utility method meant to split the option values of the additional project data
   * mapping defined in this provider and remove the escaping. These values can be accessed by using
   * the key values defined in this class.
   *
   * @param options string containing a number of options
   * @return a list of all options contained in the passed string or <code>null</code> if the passed
   *     option string is <code>null</code>
   * @see ProjectNegotiationData#getAdditionalProjectData()
   */
  @Nullable
  static String[] split(@Nullable String options) {
    if (options == null) {
      return null;
    }

    String[] splitOptions = SPLIT_PATTERN.split(options);

    for (int i = 0; i < splitOptions.length; i++) {
      splitOptions[i] = unescape(splitOptions[i]);
    }

    return splitOptions;
  }

  @NotNull
  private static String escape(@NotNull String value) {
    return value.replace(DELIMITER, ESCAPE_SEQUENCE);
  }

  @NotNull
  private static String unescape(@NotNull String value) {
    return value.replace(ESCAPE_SEQUENCE, DELIMITER);
  }
}
