package saros.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

public class ArrayUtilsTest {

  private IAdapterManager adapterManager;

  @Before
  public void createMock() {
    adapterManager = EasyMock.createMock(IAdapterManager.class);
    EasyMock.expect(
            adapterManager.getAdapter(EasyMock.anyObject(), EasyMock.anyObject(Class.class)))
        .andAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                return EasyMock.getCurrentArguments()[0];
              }
            })
        .anyTimes();

    EasyMock.replay(adapterManager);
  }

  @Test
  public void testGetInstances() {

    List<Object> objects = new ArrayList<Object>();

    objects.add(1);
    objects.add(5);
    objects.add((short) 1);
    objects.add(1.0f);
    objects.add(1.0);

    List<Number> numbers = ArrayUtils.getInstances(objects.toArray(), Number.class);

    assertEquals(objects.size(), numbers.size());

    List<Integer> integers = ArrayUtils.getInstances(objects.toArray(), Integer.class);

    assertEquals(2, integers.size());
  }

  @Test
  public void testGetInstancesNullObjects() {
    List<Object> objects = new ArrayList<Object>();

    objects.add(1);
    objects.add(5);
    objects.add(null);
    objects.add((short) 1);
    objects.add(1.0f);
    objects.add(null);
    objects.add(null);
    objects.add(1.0);

    List<Number> numbers = ArrayUtils.getInstances(objects.toArray(), Number.class);

    assertEquals(5, numbers.size());
  }

  @Test
  public void testGetInstancesReturnedList() {
    List<Object> objects = new ArrayList<Object>();

    objects.add(1);
    objects.add(5);
    objects.add((short) 1);
    objects.add(1.0f);
    objects.add(1.0);

    List<Number> numbers = ArrayUtils.getInstances(objects.toArray(), Number.class);

    numbers.add(7);
    assertTrue(numbers.contains(7));

    numbers.remove(new Integer(7));
    assertFalse(numbers.contains(7));

    int size = numbers.size();

    List<Number> temp = new ArrayList<Number>(numbers);

    numbers.addAll(temp);
    assertEquals(size * 2, numbers.size());

    temp = new ArrayList<Number>(numbers);

    numbers.removeAll(temp);

    assertEquals(0, numbers.size());
  }

  @Test
  public void testGetInstancesNullArray() {
    assertEquals(null, ArrayUtils.getInstances(null, Object.class));
  }

  @Test
  public void testGetAdaptableObjectsNullArray() {
    assertEquals(null, ArrayUtils.getAdaptableObjects(null, Object.class, null));
  }

  @Test
  public void testGetAdaptableObjects() {
    List<Object> objects = new ArrayList<Object>();
    objects.add(new Path("a"));
    objects.add(new Path("b"));
    objects.add(new Path("c"));
    objects.add(new Path("c"));

    List<IPath> resources =
        ArrayUtils.getAdaptableObjects(objects.toArray(), IPath.class, adapterManager);

    assertEquals("contains duplicates", 3, resources.size());
  }

  @Test
  public void testGetAdaptableObjectsNullObjects() {
    List<Object> objects = new ArrayList<Object>();
    objects.add(new Path("a"));
    objects.add(null);
    objects.add(new Path("b"));
    objects.add(null);
    objects.add(new Path("c"));

    List<IPath> resources =
        ArrayUtils.getAdaptableObjects(objects.toArray(), IPath.class, adapterManager);

    assertEquals("contains null objects", 3, resources.size());
  }

  @Test
  public void testGetAdaptableObjectsReturnedList() {
    List<Object> objects = new ArrayList<Object>();
    objects.add(new Path("a"));
    objects.add(null);
    objects.add(new Path("b"));
    objects.add(null);
    objects.add(new Path("c"));

    List<IPath> resources =
        ArrayUtils.getAdaptableObjects(objects.toArray(), IPath.class, adapterManager);

    resources.add(new Path("d"));
    assertTrue(resources.contains(new Path("d")));

    resources.remove(new Path("d"));
    assertFalse(resources.contains(new Path("d")));

    int size = resources.size();

    List<IPath> temp = new ArrayList<IPath>(resources);

    resources.addAll(temp);
    assertEquals(size * 2, resources.size());

    temp = new ArrayList<IPath>(resources);

    resources.removeAll(temp);

    assertEquals(0, resources.size());
  }
}
