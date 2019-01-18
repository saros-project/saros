package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.ReferencePointImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

/**
 * The IntelliJReferencePointManager maps an {@link IReferencePoint} reference point to {@link
 * Module} module
 */
public class IntelliJReferencePointManager {

  HashMap<IReferencePoint, Module> referencePointToModuleMapper;

  public IntelliJReferencePointManager() {
    referencePointToModuleMapper = new HashMap<IReferencePoint, Module>();
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link Module} module.
   * The reference point points on the module's root full path.
   *
   * @param module
   * @return the reference point of given module
   */
  public static IReferencePoint create(Module module) {
    if (module == null) return null;

    VirtualFile moduleRoot = getModuleRoot(module);

    if (moduleRoot.getPath().isEmpty()) return null;

    IPath path = IntelliJPathImpl.fromString(moduleRoot.getPath());

    return new ReferencePointImpl(path);
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link VirtualFile}
   * virtual file. The reference point points on the module's root full path.
   *
   * @param virtualFile
   * @return the reference point of given virtual file
   */
  public static IReferencePoint create(VirtualFile virtualFile) {
    if (virtualFile == null) return null;

    Module module = FilesystemUtils.getModuleOfFile(virtualFile);

    return create(module);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link Module} module The
   * reference point will created by the IntelliJPointManager.
   *
   * @param module the value of the pair
   */
  public synchronized void put(Module module) {
    IReferencePoint referencePoint = create(module);

    if (referencePoint == null) throw new IllegalStateException("Reference point can't be null");

    put(referencePoint, module);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link Module} module
   *
   * @param referencePoint the key of the pair
   * @param module the value of the pair
   */
  public synchronized void put(@NotNull IReferencePoint referencePoint, Module module) {

    if (referencePointToModuleMapper.containsKey(referencePoint))
      referencePointToModuleMapper.remove(referencePoint);

    referencePointToModuleMapper.put(referencePoint, module);
  }

  /**
   * Returns the {@link VirtualFile} module root given by the {@link IReferencePoint} reference
   * point, or null if the module of given reference point has not exactly one content root
   *
   * @param referencePoint the key for which the module should be returned
   * @return the module root given by referencePoint
   */
  public synchronized VirtualFile getModuleRoot(IReferencePoint referencePoint) {
    Module module = get(referencePoint);

    return getModuleRoot(module);
  }

  /**
   * Returns the {@link Module} given by the {@link IReferencePoint}
   *
   * @param referencePoint the key for which the module should be returned
   * @return the module given by referencePoint
   */
  public synchronized Module get(IReferencePoint referencePoint) {
    Module module = referencePointToModuleMapper.get(referencePoint);

    return module;
  }

  /**
   * Returns the {@link IResource} resource in combination of the {@link IReferencePoint} reference
   * point and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath
   */
  public synchronized VirtualFile getResource(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    Module module = get(referencePoint);

    if (module == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a module.");

    return findVirtualFile(module, referencePointRelativePath);
  }

  private VirtualFile findVirtualFile(final Module module, IPath path) {

    VirtualFile moduleRoot = getModuleRoot(module);

    if (path.isAbsolute()) return null;

    if (path.segmentCount() == 0) return moduleRoot;

    VirtualFile virtualFile = moduleRoot.findFileByRelativePath(path.toString());

    if (virtualFile != null
        && ModuleRootManager.getInstance(module).getFileIndex().isInContent(virtualFile)) {
      return virtualFile;
    }

    return null;
  }

  private static VirtualFile getModuleRoot(Module module) {
    if (module == null) return null;

    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

    VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

    int numberOfContentRoots = contentRoots.length;

    if (numberOfContentRoots != 1) {
      return null;
    }

    VirtualFile moduleRoot = contentRoots[0];

    return moduleRoot;
  }

  public void refresh(Module module) {
    if (!module.isDisposed() || !referencePointToModuleMapper.containsValue(module)) return;

    Project project = module.getProject();

    module = ModuleManager.getInstance(project).findModuleByName(module.getName());

    put(module);
  }
}
