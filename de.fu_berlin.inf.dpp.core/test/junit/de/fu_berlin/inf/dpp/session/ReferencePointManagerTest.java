package de.fu_berlin.inf.dpp.session;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReferencePointManagerTest {

  private IReferencePointManager referencePointManager;
  private IReferencePoint referencePoint;
  private IProject project;

  @Before
  public void prepare() {
    referencePoint = createMock(IReferencePoint.class);
    project = createMock(IProject.class);

    replay(referencePoint, project);

    referencePointManager = new ReferencePointManager();
  }

  @Test
  public void testPutOnce() {
    referencePointManager.put(referencePoint, project);
    IProject mappedProject = referencePointManager.get(referencePoint);

    Assert.assertNotNull(mappedProject);
    Assert.assertEquals(project, mappedProject);
  }

  @Test
  public void testGetWithoutMapping() {
    IProject mappedProject = referencePointManager.get(referencePoint);
    Assert.assertNull(mappedProject);
  }

  @Test
  public void testPutMultiplePutWithSameReferencePoint() {
    IProject newProject = createMock(IProject.class);
    replay(newProject);

    referencePointManager.put(referencePoint, project);
    referencePointManager.put(referencePoint, newProject);

    IProject mappedProject = referencePointManager.get(referencePoint);

    Assert.assertNotNull(mappedProject);
    Assert.assertEquals(newProject, mappedProject);
  }

  @Test
  public void testGetProjectSet() {
    Set<IReferencePoint> referencePoints = new HashSet<>();
    Set<IProject> projects = referencePointManager.getProjects(referencePoints);

    Assert.assertEquals(0, projects.size());

    referencePoints.add(referencePoint);
    referencePointManager.put(referencePoint, project);

    projects = referencePointManager.getProjects(referencePoints);

    Assert.assertEquals(1, projects);

    IProject mappedProject = (IProject) projects.toArray()[0];

    Assert.assertEquals(project, mappedProject);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putMappingWithNullProject() {
    try {
      referencePointManager.put(referencePoint, null);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void putMappingWithNullReferencePoint() {
    referencePointManager.put(null, project);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putGetWithNullReferencePoint() {
    referencePointManager.get(referencePoint);
  }

  @Test(expected = IllegalArgumentException.class)
  public void putGetWithNullReferencePointsSet() {
    referencePointManager.getProjects(null);
  }
}
