package saros.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IProject;
import saros.filesystem.IResource;

public class SharedProjectMapperTest {

  private SharedProjectMapper mapper;

  @Before
  public void setUp() {
    mapper = new SharedProjectMapper();
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullProject() {
    mapper.addProject("0", null);
  }

  @Test(expected = NullPointerException.class)
  public void testAddProjectWithNullID() {
    mapper.addProject(null, createProjectMock());
  }

  @Test
  public void testAddCompletelySharedProject() {
    IProject projectMock = createProjectMock();

    mapper.addProject("0", projectMock);

    assertTrue("project is not shared at all", mapper.isShared(projectMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddCompletelySharedProjectTwice() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addProject("0", projectMock);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddSameProjectWithDifferentID() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addProject("1", projectMock);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddNewProjectWithIDAlreadyInUse() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    try {
      mapper.addProject("0", projectMockA);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addProject("0", projectMockB);
  }

  @Test
  public void testRemoveProjects() {
    IProject projectMockA = createProjectMock();

    mapper.addProject("0", projectMockA);

    mapper.removeProject("0");

    assertFalse("project is still shared", mapper.isShared(projectMockA));
  }

  @Test
  public void testIgnoredResourcesOnCompletelySharedProject() {
    IProject projectMock = createProjectMock();

    IResource resourceMock = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMock.getProject()).andStubReturn(projectMock);
    EasyMock.expect(resourceMock.isIgnored()).andReturn(true);

    EasyMock.replay(resourceMock);

    mapper.addProject("0", projectMock);

    assertFalse("ignored resource is marked as shared", mapper.isShared(resourceMock));

    EasyMock.verify(resourceMock);
  }

  @Test
  public void testIsShared() {

    IProject projectMockA = createProjectMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getProject()).andStubReturn(projectMockA);

    EasyMock.replay(resourceMockA);

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockA));

    mapper.addProject("0", projectMockA);

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockA));
  }

  @Test
  public void testGetProjects() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    mapper.addProject("0", projectMockA);
    mapper.addProject("1", projectMockB);

    assertEquals(2, mapper.getProjects().size());
    assertEquals(2, mapper.size());
  }

  @Test
  public void testIDToProjectMapping() {
    IProject projectMock = createProjectMock();
    mapper.addProject("0", projectMock);
    assertEquals("0", mapper.getID(projectMock));
    assertEquals(projectMock, mapper.getProject("0"));
  }

  /*
   * aware that misconfigured mocks may throw IllegalState and
   * IllegalArgumentExceptions as well which may lead to false positive
   * (passed) test cases
   */
  private IProject createProjectMock() {
    IProject projectMock = EasyMock.createNiceMock(IProject.class);
    EasyMock.expect(projectMock.getType()).andStubReturn(IResource.PROJECT);
    EasyMock.replay(projectMock);
    return projectMock;
  }
}
