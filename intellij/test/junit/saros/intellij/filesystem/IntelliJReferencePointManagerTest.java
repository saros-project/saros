package saros.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.intellij.project.filesystem.IntelliJPathImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModuleRootManager.class)
public class IntelliJReferencePointManagerTest {

  IntelliJReferencePointManager intelliJReferencePointManager;
  IReferencePoint referencePoint;

  @Before
  public void prepare() {
    referencePoint = EasyMock.createMock(IReferencePoint.class);
    EasyMock.replay(referencePoint);

    intelliJReferencePointManager = new IntelliJReferencePointManager();
  }

  @Test
  public void testCreateReferencePoint() {
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(createModule("Module1"));
    Assert.assertNotNull(referencePoint);
  }

  @Test
  public void testModulePutIfAbsent() {
    Module module = createModule("Module1");
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);
    intelliJReferencePointManager.putIfAbsent(module);
    Module module2 = intelliJReferencePointManager.getModule(referencePoint);

    Assert.assertNotNull(module2);
    Assert.assertEquals(module, module2);
  }

  @Test
  public void testPairPutIfAbsent() {
    Module module = createModule("Module1");
    intelliJReferencePointManager.putIfAbsent(referencePoint, module);
    Module module2 = intelliJReferencePointManager.getModule(referencePoint);

    Assert.assertNotNull(module2);
    Assert.assertEquals(module, module2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetResourceFromEmptyIntelliJReferencePointManager() {
    VirtualFile resource = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(resource);

    IPath relativePath = createReferencePointPath("./path/to/file");

    intelliJReferencePointManager.getResource(referencePoint, relativePath);
  }

  private IPath createReferencePointPath(String path) {
    return IntelliJPathImpl.fromString(path);
  }

  private VirtualFile createModuleRoot(String pathToFile, VirtualFile resource) {
    VirtualFile moduleRoot = EasyMock.createMock(VirtualFile.class);
    EasyMock.expect(moduleRoot.findFileByRelativePath(pathToFile)).andStubReturn(resource);

    EasyMock.replay(moduleRoot);

    return moduleRoot;
  }

  private Module createModule(String name) {
    VirtualFile file = EasyMock.createMock(VirtualFile.class);
    EasyMock.replay(file);

    String pathToFile = "foo/bar";

    return createModule(name, file, pathToFile);
  }

  private Module createModule(String name, VirtualFile resource, String pathToFile) {
    ModuleFileIndex o = EasyMock.createMock(ModuleFileIndex.class);
    EasyMock.replay(o);

    ModuleRootManager moduleRootManager = EasyMock.createMock(ModuleRootManager.class);
    EasyMock.expect(moduleRootManager.getContentRoots())
        .andStubReturn(new VirtualFile[] {createModuleRoot(pathToFile, resource)});
    EasyMock.expect(moduleRootManager.getFileIndex()).andStubReturn(o);
    EasyMock.replay(moduleRootManager);

    Module module = EasyMock.createMock(Module.class);
    EasyMock.expect(module.getComponent(ModuleRootManager.class)).andStubReturn(moduleRootManager);
    EasyMock.expect(module.getName()).andStubReturn(name);
    EasyMock.expect(module.getModuleFilePath()).andStubReturn(name);
    EasyMock.replay(module);

    return module;
  }
}
