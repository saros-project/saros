package de.fu_berlin.inf.dpp.intellij.project.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IntelliJReferencePointManagerTest {

  IReferencePoint referencePoint;
  IPath fileReferencePointRelativePath;
  IPath folderReferencePointRelativePath;
  IPath projectFullPath;
  IntelliJReferencePointManager intelliJReferencePointManager;
  VirtualFile file;
  VirtualFile folder;
  VirtualFile moduleRoot;
  Module module;
  String moduleRootPath;
  ModuleRootManager manager;

  @Before
  public void setup() {
    moduleRootPath = "path/to/foo";

    fileReferencePointRelativePath = EasyMock.createMock(IPath.class);
    EasyMock.expect(fileReferencePointRelativePath.isAbsolute()).andStubReturn(false);
    EasyMock.expect(fileReferencePointRelativePath.segmentCount()).andStubReturn(2);
    // EasyMock.expect(fileReferencePointRelativePath).andStubReturn("/path");
    folderReferencePointRelativePath = EasyMock.createMock(IPath.class);
    projectFullPath = EasyMock.createMock(IPath.class);
    referencePoint = EasyMock.createMock(IReferencePoint.class);
    file = EasyMock.createMock(VirtualFile.class);
    folder = EasyMock.createMock(VirtualFile.class);

    EasyMock.replay(
        fileReferencePointRelativePath,
        folderReferencePointRelativePath,
        file,
        folder,
        referencePoint);

    moduleRoot = EasyMock.createMock(VirtualFile.class);
    EasyMock.expect(moduleRoot.getPath()).andStubReturn(moduleRootPath);
    EasyMock.expect(moduleRoot.findFileByRelativePath("/path")).andStubReturn(file);

    EasyMock.replay(moduleRoot);

    manager = EasyMock.createMock(ModuleRootManager.class);
    EasyMock.expect(manager.getContentRoots()).andStubReturn(new VirtualFile[] {moduleRoot});
    EasyMock.replay(manager);

    module = EasyMock.createMock(Module.class);
    EasyMock.expect(module.getComponent(ModuleRootManager.class)).andStubReturn(manager);
    EasyMock.replay(module);
    intelliJReferencePointManager = new IntelliJReferencePointManager();
  }

  @Test
  public void testCreateReferencePoint() {
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);
    Assert.assertNotNull(referencePoint);
  }

  @Test
  public void testPutIfAbsent() {
    IReferencePoint referencePoint = IntelliJReferencePointManager.create(module);
    intelliJReferencePointManager.put(referencePoint, module);
    Module m = intelliJReferencePointManager.get(referencePoint);

    Assert.assertNotNull(m);
  }
}
