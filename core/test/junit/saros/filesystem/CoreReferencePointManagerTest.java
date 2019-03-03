package saros.filesystem;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import saros.activities.SPath;

public class CoreReferencePointManagerTest {

  private final String PROJECT_RELATIVE_PATH_TO_FILE = "/toFile";
  private final String PROJECT_RELATIVE_PATH_TO_FOLDER = "/toFolder/";
  private final String PROJECT_NAME = "Bar";
  private final String CHARSET = "UTF-8";

  private IReferencePointManager referencePointManager;
  private IReferencePoint referencePoint;
  private IProject project;
  private IFolder folder;
  private IFile file;
  private IPath projectRelativePath;

  @Before
  public void setup() throws IOException {
    file = createMock(IFile.class);
    folder = createMock(IFolder.class);
    projectRelativePath = createMock(IPath.class);
    expect(projectRelativePath.isAbsolute()).andStubReturn(false);

    replay(file, folder, projectRelativePath);

    referencePoint = createMock(IReferencePoint.class);

    project = createMock(IProject.class);
    expect(project.exists()).andStubReturn(true);
    expect(project.getFile(PROJECT_RELATIVE_PATH_TO_FILE)).andStubReturn(file);
    expect(project.getFolder(PROJECT_RELATIVE_PATH_TO_FOLDER)).andStubReturn(folder);
    expect(project.getName()).andStubReturn(PROJECT_NAME);
    expect(project.getDefaultCharset()).andStubReturn(CHARSET);
    expect(project.members()).andStubReturn(new IResource[] {file, folder});
    replay(referencePoint, project);

    referencePointManager = new CoreReferencePointManager();
  }

  @Test
  public void testPutOnce() {
    referencePointManager.put(referencePoint, project);
    IProject mappedProject = referencePointManager.getProject(referencePoint);

    Assert.assertNotNull(mappedProject);
    Assert.assertEquals(project, mappedProject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetWithoutMapping() {
    referencePointManager.getProject(referencePoint);
  }

  @Test
  public void testPutMultiplePutWithSameReferencePoint() {
    IProject newProject = createMock(IProject.class);
    replay(newProject);

    referencePointManager.put(referencePoint, project);
    referencePointManager.put(referencePoint, newProject);

    IProject mappedProject = referencePointManager.getProject(referencePoint);

    Assert.assertNotNull(mappedProject);
    Assert.assertEquals(project, mappedProject);
  }

  @Test
  public void testGetProjectSet() {
    Set<IReferencePoint> referencePoints = new HashSet<>();
    Set<IProject> projects = referencePointManager.getProjects(referencePoints);

    Assert.assertEquals(0, projects.size());

    referencePoints.add(referencePoint);
    referencePointManager.put(referencePoint, project);

    projects = referencePointManager.getProjects(referencePoints);

    Assert.assertEquals(1, projects.size());

    IProject mappedProject = (IProject) projects.toArray()[0];

    Assert.assertEquals(project, mappedProject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putMappingWithNullProject() {
    referencePointManager.put(referencePoint, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putMappingWithNullReferencePoint() {
    referencePointManager.put(null, project);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putGetWithNullReferencePoint() {
    referencePointManager.getProject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putGetWithNullReferencePointsSet() {
    referencePointManager.getProjects(null);
  }

  @Test
  public void testGetFolder() {
    referencePointManager.put(referencePoint, project);
    IFolder testFolder =
        referencePointManager.getFolder(referencePoint, PROJECT_RELATIVE_PATH_TO_FOLDER);

    Assert.assertEquals(folder, testFolder);
  }

  @Test
  public void testGetFile() {
    referencePointManager.put(referencePoint, project);
    IFile testFile = referencePointManager.getFile(referencePoint, PROJECT_RELATIVE_PATH_TO_FILE);

    Assert.assertEquals(file, testFile);
  }

  @Test
  public void testProjectExist() {
    referencePointManager.put(referencePoint, project);
    boolean projectExists = referencePointManager.projectExists(referencePoint);

    Assert.assertTrue(projectExists);
  }

  @Test
  public void testGetName() {
    referencePointManager.put(referencePoint, project);
    String testName = referencePointManager.getName(referencePoint);

    Assert.assertEquals(PROJECT_NAME, testName);
  }

  @Test
  public void testGetDefaultCharset() throws IOException {
    referencePointManager.put(referencePoint, project);
    String testCharset = referencePointManager.getDefaultCharSet(referencePoint);

    Assert.assertEquals(CHARSET, testCharset);
  }

  @Test
  public void testGetMembers() throws IOException {
    referencePointManager.put(referencePoint, project);
    IResource[] members = referencePointManager.members(referencePoint);

    Assert.assertEquals(2, members.length);

    List<IResource> membersList = new ArrayList<IResource>(Arrays.asList(members));

    Assert.assertTrue(membersList.contains(file));
    Assert.assertTrue(membersList.contains(folder));
  }

  @Test
  public void testCreateSPath() {
    referencePointManager.put(referencePoint, project);

    SPath expectedSPath = new SPath(project, projectRelativePath);

    SPath testSPath = referencePointManager.createSPath(referencePoint, projectRelativePath);

    Assert.assertEquals(expectedSPath, testSPath);
  }
}
