package de.fu_berlin.inf.dpp.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SharedReferencePointMapperTest {

  private SharedReferencePointMapper mapper;

  @Before
  public void setUp() {
    mapper = new SharedReferencePointMapper();
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullReferencePoint() {
    mapper.addReferencePoint("0", null, false);
  }

  @Test(expected = NullPointerException.class)
  public void testAddReferencePointWithNullID() {
    mapper.addReferencePoint(null, createReferencePointMock(), false);
  }

  @Test
  public void testAddCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMock, false);

    assertTrue("referencePoint is not shared at all", mapper.isShared(referencePointMock));

    assertFalse("referencePoint is partially shared", mapper.isPartiallyShared(referencePointMock));

    assertTrue(
        "referencePoint is not completely shared", mapper.isCompletelyShared(referencePointMock));
  }

  @Test
  public void testAddPartiallySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMock, true);

    assertTrue("referencePoint is not shared at all", mapper.isShared(referencePointMock));

    assertFalse(
        "referencePoint is completely shared", mapper.isCompletelyShared(referencePointMock));

    assertTrue(
        "referencePoint is not partially shared", mapper.isPartiallyShared(referencePointMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddCompletelySharedReferencePointTwice() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock, false);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addReferencePoint("0", referencePointMock, false);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddPartiallySharedReferencePointTwice() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addReferencePoint("0", referencePointMock, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddSameReferencePointWithDifferentID() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addReferencePoint("1", referencePointMock, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddNewReferencePointWithIDAlreadyInUse() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMockA, true);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addReferencePoint("0", referencePointMockB, true);
  }

  @Test
  public void testPartiallySharedReferencePointUpgrade() {
    IReferencePoint referencePointMock = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMock, true);
    assertTrue(
        "referencePoint is not partially shared", mapper.isPartiallyShared(referencePointMock));

    assertFalse(
        "referencePoint is completely shared", mapper.isCompletelyShared(referencePointMock));

    mapper.addReferencePoint("0", referencePointMock, false);
    assertFalse("referencePoint is partially shared", mapper.isPartiallyShared(referencePointMock));

    assertTrue(
        "referencePoint is not completely shared", mapper.isCompletelyShared(referencePointMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testCompletelySharedReferencePointDowngrade() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock, false);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addReferencePoint("0", referencePointMock, true);
  }

  @Test
  public void testRemoveReferencePoints() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMockA, false);
    mapper.addReferencePoint("1", referencePointMockB, true);

    mapper.removeReferencePoint("0");
    mapper.removeReferencePoint("1");

    assertFalse("referencePoint is still shared", mapper.isShared(referencePointMockA));
    assertFalse("referencePoint is still shared", mapper.isShared(referencePointMockB));

    assertFalse(
        "referencePoint is still completely shared",
        mapper.isCompletelyShared(referencePointMockA));
    assertFalse(
        "referencePoint is still partially shared", mapper.isPartiallyShared(referencePointMockB));
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testAddResourcesToCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();
    mapper.addReferencePoint("0", referencePointMock, false);
    List<IResource> emptyList = Collections.emptyList();
    mapper.addResources(referencePointMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testAddResourcesToNonSharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    List<IResource> emptyList = Collections.emptyList();
    mapper.addResources(referencePointMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testRemoveResourcesFromCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMock, false);

    List<IResource> emptyList = Collections.emptyList();
    mapper.removeResources(referencePointMock, emptyList);
  }

  @Test(expected = IllegalStateException.class)
  @Ignore(
      "logic is currently not performed - should be enabled after the SarosSession is properly synchronized")
  public void testRemoveResourcesFromNonSharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    List<IResource> emptyList = Collections.emptyList();
    mapper.removeResources(referencePointMock, emptyList);
  }

  @Test
  public void testAddRemoveResourcesOfPartiallySharedReferencePoint() {

    IReferencePoint referencePointMock = createReferencePointMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getReferencePoint()).andStubReturn(referencePointMock);
    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getReferencePoint()).andStubReturn(referencePointMock);
    EasyMock.replay(resourceMockA, resourceMockB);

    mapper.addReferencePoint("0", referencePointMock, true);
    mapper.addResources(referencePointMock, Collections.singletonList(resourceMockA));

    assertTrue("resource is not shared", mapper.isShared(resourceMockA, referencePointMock));
    assertEquals(1, mapper.getPartiallySharedResources().size());

    mapper.removeResources(referencePointMock, Collections.singletonList(resourceMockA));

    assertFalse("resource is still shared", mapper.isShared(resourceMockA, referencePointMock));
    assertEquals(0, mapper.getPartiallySharedResources().size());

    mapper.addResources(referencePointMock, Collections.singletonList(resourceMockA));

    mapper.removeAndAddResources(
        referencePointMock,
        Collections.singletonList(resourceMockA),
        Collections.singletonList(resourceMockB));

    assertFalse("resource is still shared", mapper.isShared(resourceMockA, referencePointMock));
    assertTrue("resource is not shared", mapper.isShared(resourceMockB, referencePointMock));
    assertEquals(1, mapper.getPartiallySharedResources().size());
  }

  @Test
  public void testDerivedResourcesOnCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    IResource resourceMock = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMock.getReferencePoint()).andStubReturn(referencePointMock);
    EasyMock.expect(resourceMock.isDerived(true)).andReturn(true);

    EasyMock.replay(resourceMock);

    mapper.addReferencePoint("0", referencePointMock, false);

    assertFalse("derived resource is marked as shared", mapper.isShared(resourceMock, referencePointMock));

    EasyMock.verify(resourceMock);
  }

  @Test
  public void testIsShared() {

    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getReferencePoint()).andStubReturn(referencePointMockA);

    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getReferencePoint()).andStubReturn(referencePointMockB);

    EasyMock.replay(resourceMockA, resourceMockB);

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockA, referencePointMockA));

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockB, referencePointMockB));

    mapper.addReferencePoint("0", referencePointMockA, false);
    mapper.addReferencePoint("1", referencePointMockB, true);

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockA, referencePointMockA));

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockB, referencePointMockB));

    mapper.addResources(referencePointMockB, Collections.singletonList(resourceMockB));

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockB, referencePointMockB));
  }

  @Test
  public void testGetReferencePointResourceMapping() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    IResource resourceMockB = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockB.getReferencePoint()).andStubReturn(referencePointMockB);

    EasyMock.replay(resourceMockB);

    mapper.addReferencePoint("0", referencePointMockA, false);
    mapper.addReferencePoint("1", referencePointMockB, true);

    mapper.addResources(referencePointMockB, Collections.singletonList(resourceMockB));

    Map<IReferencePoint, List<IResource>> mapping = mapper.getReferencePointResourceMapping();

    assertNull(
        "completely shared referencePoint have no resource list", mapping.get(referencePointMockA));

    assertNotNull(
        "partially shared referencePoint must have a resource list",
        mapping.get(referencePointMockB));

    assertEquals(
        "resource list does not contain the shared resource",
        1,
        mapping.get(referencePointMockB).size());
  }

  @Test
  public void testGetReferencePoints() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMockA, false);
    mapper.addReferencePoint("1", referencePointMockB, false);

    assertEquals(2, mapper.getReferencePoints().size());
    assertEquals(2, mapper.size());
  }

  @Test
  public void testIDToReferencePointMapping() {
    IReferencePoint referencePointMock = createReferencePointMock();
    mapper.addReferencePoint("0", referencePointMock, false);
    assertEquals("0", mapper.getID(referencePointMock));
    assertEquals(referencePointMock, mapper.getReferencePoint("0"));
  }

  /*
   * aware that misconfigured mocks may throw IllegalState and
   * IllegalArgumentExceptions as well which may lead to false positive
   * (passed) test cases
   */
  private IReferencePoint createReferencePointMock() {
    IReferencePoint referencePointMock = EasyMock.createNiceMock(IReferencePoint.class);
    EasyMock.replay(referencePointMock);
    return referencePointMock;
  }
}
