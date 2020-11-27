package saros.negotiation.additional_resource_data;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import saros.filesystem.IReferencePoint;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.negotiation.AdditionalResourceDataFactory;
import saros.util.PathUtils;

/**
 * Intellij class providing possible representations of the shared reference point by using the
 * module name and content-root-relative path of the reference point delegate.
 *
 * <p>This possible representation is provided for all possible modules, not just the closest one.
 * I.e. if the reference point resource is located in a nested module, the possible representation
 * for the module and the parent module is provided. This should ensure that good local mapping
 * suggestions are still possible, even if the Intellij module setup is more complicated than the
 * resource model on the receiving side (e.g. when using gradle).
 */
public class IntellijPossibleRepresentationProvider extends AbstractPossibleRepresentationProvider {

  public IntellijPossibleRepresentationProvider(
      AdditionalResourceDataFactory additionalResourceDataFactory) {

    super(additionalResourceDataFactory);
  }

  @Override
  protected List<Pair<String, String>> computePossibleRepresentations(
      IReferencePoint referencePoint) {
    IntellijReferencePoint intellijReferencePoint = (IntellijReferencePoint) referencePoint;

    Project project = intellijReferencePoint.getProject();

    VirtualFile referencePointVirtualFile = intellijReferencePoint.getVirtualFile();
    Path referencePointPath = Paths.get(referencePointVirtualFile.getPath());

    List<Pair<String, String>> possibleRepresentations = new ArrayList<>();

    Module module =
        ProjectRootManager.getInstance(project)
            .getFileIndex()
            .getModuleForFile(referencePointVirtualFile, false);
    VirtualFile contentRoot =
        ProjectRootManager.getInstance(project)
            .getFileIndex()
            .getContentRootForFile(referencePointVirtualFile, false);

    while (module != null && contentRoot != null) {
      assert Arrays.asList(ModuleRootManager.getInstance(module).getContentRoots())
          .contains(contentRoot);

      String moduleName = module.getName();

      Path contentRootPath = Paths.get(contentRoot.getPath());
      Path contentRootRelativePath = contentRootPath.relativize(referencePointPath);

      String portableContentRootRelativePath = PathUtils.toPortableString(contentRootRelativePath);

      possibleRepresentations.add(new ImmutablePair<>(moduleName, portableContentRootRelativePath));

      VirtualFile parentFile = contentRoot.getParent();
      if (parentFile == null) {
        break;
      }

      module =
          ProjectRootManager.getInstance(project)
              .getFileIndex()
              .getModuleForFile(parentFile, false);
      contentRoot =
          ProjectRootManager.getInstance(project)
              .getFileIndex()
              .getContentRootForFile(parentFile, false);
    }

    return possibleRepresentations;
  }
}
