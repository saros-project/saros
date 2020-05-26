package saros.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static saros.filesystem.IResource.Type.REFERENCE_POINT;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;

public class SharedReferencePointMapperTest {

  private SharedReferencePointMapper mapper;

  @Before
  public void setUp() {
    mapper = new SharedReferencePointMapper();
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullReferencePoint() {
    mapper.addReferencePoint("0", null);
  }

  @Test(expected = NullPointerException.class)
  public void testAddReferencePointWithNullID() {
    mapper.addReferencePoint(null, createReferencePointMock());
  }

  @Test
  public void testAddCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMock);

    assertTrue("reference point is not shared at all", mapper.isShared(referencePointMock));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddCompletelySharedReferencePointTwice() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    mapper.addReferencePoint("0", referencePointMock);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddSameReferencePointWithDifferentID() {
    IReferencePoint referencePointMock = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMock);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addReferencePoint("1", referencePointMock);
  }

  @Test(expected = IllegalStateException.class)
  public void testAddNewReferencePointWithIDAlreadyInUse() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    try {
      mapper.addReferencePoint("0", referencePointMockA);
    } catch (RuntimeException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    mapper.addReferencePoint("0", referencePointMockB);
  }

  @Test
  public void testRemoveReferencePoints() {
    IReferencePoint referencePointMockA = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMockA);

    mapper.removeReferencePoint("0");

    assertFalse("reference point is still shared", mapper.isShared(referencePointMockA));
  }

  @Test
  public void testIgnoredResourcesOnCompletelySharedReferencePoint() {
    IReferencePoint referencePointMock = createReferencePointMock();

    IResource resourceMock = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMock.getReferencePoint()).andStubReturn(referencePointMock);
    EasyMock.expect(resourceMock.isIgnored()).andReturn(true);

    EasyMock.replay(resourceMock);

    mapper.addReferencePoint("0", referencePointMock);

    assertFalse("ignored resource is marked as shared", mapper.isShared(resourceMock));

    EasyMock.verify(resourceMock);
  }

  @Test
  public void testIsShared() {

    IReferencePoint referencePointMockA = createReferencePointMock();

    IResource resourceMockA = EasyMock.createNiceMock(IResource.class);
    EasyMock.expect(resourceMockA.getReferencePoint()).andStubReturn(referencePointMockA);

    EasyMock.replay(resourceMockA);

    assertFalse("resource should not be marked as shared", mapper.isShared(resourceMockA));

    mapper.addReferencePoint("0", referencePointMockA);

    assertTrue("resource is not marked as shared", mapper.isShared(resourceMockA));
  }

  @Test
  public void testGetReferencePoints() {
    IReferencePoint referencePointMockA = createReferencePointMock();
    IReferencePoint referencePointMockB = createReferencePointMock();

    mapper.addReferencePoint("0", referencePointMockA);
    mapper.addReferencePoint("1", referencePointMockB);

    assertEquals(2, mapper.getReferencePoints().size());
    assertEquals(2, mapper.size());
  }

  @Test
  public void testIDToReferencePointMapping() {
    IReferencePoint referencePointMock = createReferencePointMock();
    mapper.addReferencePoint("0", referencePointMock);
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
    EasyMock.expect(referencePointMock.getType()).andStubReturn(REFERENCE_POINT);
    EasyMock.replay(referencePointMock);
    return referencePointMock;
  }
}
