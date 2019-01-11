package de.fu_berlin.inf.dpp.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SharedProjectMapperTest {

  private SharedProjectMapper mapper;

  @Before
  public void setUp() {
    mapper = new SharedProjectMapper();
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullProject() {
    mapper.addProject("0", null, false);
  }

  @Test(expected = NullPointerException.class)
  public void testAddProjectWithNullID() {
    mapper.addProject(null, createProjectMock(), false);
  }

  @Test
  public void testAddCompletelySharedProject() {
    IProject projectMock = createProjectMock();

    mapper.addProject("0", projectMock, false);

    assertTrue("project is not shared at all", mapper.isShared(projectMock));

    assertFalse("project is partially shared", mapper.isPartiallyShared(projectMock));

    assertTrue("project is not completely shared", mapper.isCompletelyShared(projectMock));
  }

  @Test
  public void testAddPartiallySharedProject() {
    IProject projectMock = createProjectMock();

    mapper.addProject("0", projectMock, true);

    assertTrue("project is not shared at all", mapper.isShared(projectMock));

    assertFalse("project is completely shared", mapper.isCompletelyShared(projectMock));

    assertTrue("project is not partially shared", mapper.isPartiallyShared(projectMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddCompletelySharedProjectTwice() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock, false);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addProject("0", projectMock, false);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddPartiallySharedProjectTwice() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addProject("0", projectMock, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddSameProjectWithDifferentID() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addProject("1", projectMock, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddNewProjectWithIDAlreadyInUse() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    try {
      mapper.addProject("0", projectMockA, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addProject("0", projectMockB, true);
  }

  @Test
  public void testPartiallySharedProjectUpgrade() {
    IProject projectMock = createProjectMock();

    mapper.addProject("0", projectMock, true);
    assertTrue("project is not partially shared", mapper.isPartiallyShared(projectMock));

    assertFalse("project is completely shared", mapper.isCompletelyShared(projectMock));

    mapper.addProject("0", projectMock, false);
    assertFalse("project is partially shared", mapper.isPartiallyShared(projectMock));

    assertTrue("project is not completely shared", mapper.isCompletelyShared(projectMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testCompletelySharedProjectDowngrade() {
    IProject projectMock = createProjectMock();

    try {
      mapper.addProject("0", projectMock, false);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addProject("0", projectMock, true);
  }

  @Test
  public void testRemoveProjects() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    mapper.addProject("0", projectMockA, false);
    mapper.addProject("1", projectMockB, true);

    mapper.removeProject("0");
    mapper.removeProject("1");

    assertFalse("project is still shared", mapper.isShared(projectMockA));
    assertFalse("project is still shared", mapper.isShared(projectMockB));

    assertFalse("project is still completely shared", mapper.isCompletelyShared(projectMockA));
    assertFalse("project is still partially shared", mapper.isPartiallyShared(projectMockB));
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testAddResourcesToCompletelySharedProject() {
    IProject projectMock = createProjectMock();
    mapper.addProject("0", projectMock, false);
    List<IResource> emptyList = Collections.emptyList();
    mapper.addResources(projectMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testAddResourcesToNonSharedProject() {
    IProject projectMock = createProjectMock();

    List<IResource> emptyList = Collections.emptyList();
    mapper.addResources(projectMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testRemoveResourcesFromCompletelySharedProject() {
    IProject projectMock = createProjectMock();

    mapper.addProject("0", projectMock, false);

    List<IResource> emptyList = Collections.emptyList();
    mapper.removeResources(projectMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testRemoveResourcesFromNonSharedProject() {
    IProject projectMock = createProjectMock();

    List<IResource> emptyList = Collections.emptyList();
    mapper.removeResources(projectMock, emptyList);
  }

  @Test
  public void testAddRemoveResourcesOfPartiallySharedProject() {

    IProject projectMock = createProjectMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getProject()).andStubReturn(projectMock);
    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getProject()).andStubReturn(projectMock);
    EasyMock.replay(resourceMockA, resourceMockB);

    mapper.addProject("0", projectMock, true);
    mapper.addResources(projectMock, Collections.singletonList(resourceMockA));

    assertTrue("resource is not shared", mapper.isShared(resourceMockA));
    assertEquals(1, mapper.getPartiallySharedResources().size());

    mapper.removeResources(projectMock, Collections.singletonList(resourceMockA));

    assertFalse("resource is still shared", mapper.isShared(resourceMockA));
    assertEquals(0, mapper.getPartiallySharedResources().size());

    mapper.addResources(projectMock, Collections.singletonList(resourceMockA));

    mapper.removeAndAddResources(
        projectMock,
        Collections.singletonList(resourceMockA),
        Collections.singletonList(resourceMockB));

    assertFalse("resource is still shared", mapper.isShared(resourceMockA));
    assertTrue("resource is not shared", mapper.isShared(resourceMockB));
    assertEquals(1, mapper.getPartiallySharedResources().size());
  }

  @Test
  public void testDerivedResourcesOnCompletelySharedProject() {
    IProject projectMock = createProjectMock();

    IResource resourceMock = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMock.getProject()).andStubReturn(projectMock);
    EasyMock.expect(resourceMock.isDerived(true)).andReturn(true);

    EasyMock.replay(resourceMock);

    mapper.addProject("0", projectMock, false);

    assertFalse("derived resource is marked as shared", mapper.isShared(resourceMock));

    EasyMock.verify(resourceMock);
  }

  @Test
  public void testIsShared() {

    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getProject()).andStubReturn(projectMockA);

    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getProject()).andStubReturn(projectMockB);

    EasyMock.replay(resourceMockA, resourceMockB);

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockA));

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockB));

    mapper.addProject("0", projectMockA, false);
    mapper.addProject("1", projectMockB, true);

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockA));

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockB));

    mapper.addResources(projectMockB, Collections.singletonList(resourceMockB));

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockB));
  }

  @Test
  public void testGetProjectResourceMapping() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getProject()).andStubReturn(projectMockB);

    EasyMock.replay(resourceMockB);

    mapper.addProject("0", projectMockA, false);
    mapper.addProject("1", projectMockB, true);

    mapper.addResources(projectMockB, Collections.singletonList(resourceMockB));

    Map<IProject, List<IResource>> mapping = mapper.getProjectResourceMapping();

    assertNull("completely shared projects have no resource list", mapping.get(projectMockA));

    assertNotNull("partially shared projects must have a resource list", mapping.get(projectMockB));

    assertEquals(
        "resource list does not contain the shared resource", 1, mapping.get(projectMockB).size());
  }

  @Test
  public void testGetProjects() {
    IProject projectMockA = createProjectMock();
    IProject projectMockB = createProjectMock();

    mapper.addProject("0", projectMockA, false);
    mapper.addProject("1", projectMockB, false);

    assertEquals(2, mapper.getProjects().size());
    assertEquals(2, mapper.size());
  }

  @Test
  public void testIDToProjectMapping() {
    IProject projectMock = createProjectMock();
    mapper.addProject("0", projectMock, false);
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
