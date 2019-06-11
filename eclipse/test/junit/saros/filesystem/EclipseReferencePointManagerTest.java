package saros.filesystem;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import saros.activities.SPath;

public class EclipseReferencePointManagerTest {

  IReferencePoint referencePoint;
  EclipseReferencePointManager eclipseReferencePointManager;

  @Before
  public void prepare() {
    referencePoint = EasyMock.createMock(IReferencePoint.class);
    EasyMock.replay(referencePoint);

    eclipseReferencePointManager = new EclipseReferencePointManager();
  }

  @Test
  public void testPutIfAbsent() {
    IProject project = createProjectMock();
    EasyMock.replay(project);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IProject projectFromReferencePointManager =
        eclipseReferencePointManager.getProject(referencePoint);

    Assert.assertNotNull(projectFromReferencePointManager);
    Assert.assertEquals(project, projectFromReferencePointManager);
  }

  @Test
  public void testFindMember() {
    IResource resource = createResourceMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.findMember(projectRelativePath)).andStubReturn(resource);
    EasyMock.replay(project);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IResource resourceFromReferencePointManager =
        eclipseReferencePointManager.findMember(referencePoint, projectRelativePath);

    Assert.assertNotNull(resource);
    Assert.assertEquals(resource, resourceFromReferencePointManager);
  }

  @Test
  public void testFindMemberWithSPath() {
    IResource resource = createResourceMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.findMember(projectRelativePath)).andStubReturn(resource);
    EasyMock.replay(project);
    SPath sPath = createSPathMock(projectRelativePath);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IResource resourceFromReferencePointManager = eclipseReferencePointManager.findMember(sPath);

    Assert.assertNotNull(resourceFromReferencePointManager);
    Assert.assertEquals(resource, resourceFromReferencePointManager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetResourceWithNullReferencePoint() {
    eclipseReferencePointManager.findMember(null, createRelativePathMock());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetResourceWithNullRelativePath() {
    eclipseReferencePointManager.findMember(referencePoint, null);
  }

  @Test
  public void testGetFile() {
    IFile file = createFileMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.getFile(projectRelativePath)).andStubReturn(file);
    EasyMock.replay(project);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IFile fileFromReferencePointManager =
        eclipseReferencePointManager.getFile(referencePoint, projectRelativePath);

    Assert.assertNotNull(fileFromReferencePointManager);
    Assert.assertEquals(file, fileFromReferencePointManager);
  }

  @Test
  public void testGetFileWithSPath() {
    IFile file = createFileMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.getFile(projectRelativePath)).andStubReturn(file);
    EasyMock.replay(project);
    SPath sPath = createSPathMock(projectRelativePath);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IFile fileFromReferencePointManager = eclipseReferencePointManager.getFile(sPath);

    Assert.assertNotNull(fileFromReferencePointManager);
    Assert.assertEquals(file, fileFromReferencePointManager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFileWithNullReferencePoint() {
    eclipseReferencePointManager.getFile(null, createRelativePathMock());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFileWithNullRelativePath() {
    eclipseReferencePointManager.getFile(referencePoint, null);
  }

  @Test
  public void testGetFolder() {
    IFolder folder = createFolderMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.getFolder(projectRelativePath)).andStubReturn(folder);
    EasyMock.replay(project);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IFolder folderFromReferencePointManager =
        eclipseReferencePointManager.getFolder(referencePoint, projectRelativePath);

    Assert.assertNotNull(folderFromReferencePointManager);
    Assert.assertEquals(folder, folderFromReferencePointManager);
  }

  @Test
  public void testGetFolderWithSPath() {
    IFolder folder = createFolderMock();
    IPath projectRelativePath = createRelativePathMock();
    IProject project = createProjectMock();
    EasyMock.expect(project.getFolder(projectRelativePath)).andStubReturn(folder);
    EasyMock.replay(project);
    SPath sPath = createSPathMock(projectRelativePath);

    eclipseReferencePointManager.putIfAbsent(referencePoint, project);

    IFolder folderFromReferencePointManager = eclipseReferencePointManager.getFolder(sPath);

    Assert.assertNotNull(folderFromReferencePointManager);
    Assert.assertEquals(folder, folderFromReferencePointManager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFolderWithNullReferencePoint() {
    eclipseReferencePointManager.getFolder(null, createRelativePathMock());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFolderWithNullRelativePath() {
    eclipseReferencePointManager.getFolder(referencePoint, null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetFolderWithSPathWithNullRelativePath() {
    SPath path = EasyMock.createMock(SPath.class);

    EasyMock.expect(path.getReferencePoint()).andStubReturn(referencePoint);
    EasyMock.expect(path.getProjectRelativePath()).andStubReturn(null);
    EasyMock.replay(path);

    eclipseReferencePointManager.getFolder(path);
  }

  private IFile createFileMock() {
    IFile file = EasyMock.createMock(IFile.class);
    EasyMock.replay(file);

    return file;
  }

  private IFolder createFolderMock() {
    IFolder folder = EasyMock.createMock(IFolder.class);
    EasyMock.replay(folder);

    return folder;
  }

  private IResource createResourceMock() {
    IResource resource = EasyMock.createMock(IResource.class);
    EasyMock.replay(resource);

    return resource;
  }

  private IProject createProjectMock() {
    IProject project = EasyMock.createMock(IProject.class);

    return project;
  }

  private IPath createRelativePathMock() {
    IPath relativePath = EasyMock.createMock(IPath.class);
    EasyMock.replay(relativePath);

    return relativePath;
  }

  private SPath createSPathMock(IPath projectRelativePath) {
    SPath sPath = EasyMock.createMock(SPath.class);
    EasyMock.expect(sPath.getReferencePoint()).andStubReturn(referencePoint);
    EasyMock.expect(sPath.getProjectRelativePath())
        .andStubReturn(createSarosIPathMock(projectRelativePath));
    EasyMock.replay(sPath);

    return sPath;
  }

  private EclipsePathImpl createSarosIPathMock(IPath projectRelativePath) {
    EclipsePathImpl path = EasyMock.createMock(EclipsePathImpl.class);
    EasyMock.expect(path.getDelegate()).andStubReturn(projectRelativePath);
    EasyMock.replay(path);

    return path;
  }
}
