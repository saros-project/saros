package de.fu_berlin.inf.dpp.filesystem;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EclipseReferencePointManagerTest {

  IProject project;
  IReferencePoint referencePoint;
  IFile file;
  IFolder folder;
  IResource resource;
  IPath fileReferencePointRelativePath;
  IPath folderReferencePointRelativePath;
  IPath projectFullPath;
  EclipseReferencePointManager eclipseReferencePointManager;

  @Before
  public void setup() {
    fileReferencePointRelativePath = EasyMock.createMock(IPath.class);
    folderReferencePointRelativePath = EasyMock.createMock(IPath.class);
    projectFullPath = EasyMock.createMock(IPath.class);

    referencePoint = EasyMock.createMock(IReferencePoint.class);

    file = EasyMock.createMock(IFile.class);
    folder = EasyMock.createMock(IFolder.class);
    resource = EasyMock.createMock(IResource.class);

    EasyMock.replay(
        fileReferencePointRelativePath,
        folderReferencePointRelativePath,
        file,
        folder,
        referencePoint);

    project = EasyMock.createMock(IProject.class);
    EasyMock.expect(project.getFullPath()).andStubReturn(projectFullPath);
    EasyMock.expect(project.findMember(fileReferencePointRelativePath)).andStubReturn(resource);
    EasyMock.expect(project.getFile(fileReferencePointRelativePath)).andStubReturn(file);
    EasyMock.expect(project.getFolder(folderReferencePointRelativePath)).andStubReturn(folder);
    EasyMock.replay(project);

    eclipseReferencePointManager = new EclipseReferencePointManager();
  }

  @Test
  public void testCreateReferencePoint() {
    IReferencePoint originReferencePoint =
        new ReferencePointImpl(ResourceAdapterFactory.create(projectFullPath));
    IResource resource = EasyMock.createMock(IResource.class);
    EasyMock.expect(resource.getProject()).andStubReturn(project);
    EasyMock.replay(resource);

    IReferencePoint ref = EclipseReferencePointManager.create(resource);
    Assert.assertEquals(originReferencePoint, ref);
  }

  @Test
  public void testPutIfAbsent() {
    eclipseReferencePointManager.put(referencePoint, project);
    IProject p = eclipseReferencePointManager.get(referencePoint);
    Assert.assertNotNull(p);
  }

  @Test
  public void testGetResource() {
    eclipseReferencePointManager.put(referencePoint, project);
    IProject p = eclipseReferencePointManager.get(referencePoint);
    IResource resource =
        eclipseReferencePointManager.getResource(referencePoint, fileReferencePointRelativePath);
    Assert.assertNotNull(resource);
  }

  @Test
  public void testGetFile() {
    eclipseReferencePointManager.put(referencePoint, project);
    IProject p = eclipseReferencePointManager.get(referencePoint);
    IFile file =
        eclipseReferencePointManager.getFile(referencePoint, fileReferencePointRelativePath);
    Assert.assertNotNull(file);
  }

  @Test
  public void testGetFolder() {
    eclipseReferencePointManager.put(referencePoint, project);
    IProject p = eclipseReferencePointManager.get(referencePoint);
    IFolder folder =
        eclipseReferencePointManager.getFolder(referencePoint, folderReferencePointRelativePath);
    Assert.assertNotNull(folder);
  }
}
