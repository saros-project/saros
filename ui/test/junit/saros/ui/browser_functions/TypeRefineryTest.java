package saros.ui.browser_functions;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Test;

public class TypeRefineryTest {

  class Dummy {
    String label;
    Dummy next;
    int age;

    public Dummy(String label, Dummy next, int age) {
      this.label = label;
      this.next = next;
      this.age = age;
    }
  }

  @Test
  public void stringProcessing() {
    TypeRefinery ref = new TypeRefinery();

    String refinedString = ref.refine("Hello World", String.class);

    assertEquals("Hello World", refinedString);
  }

  @Test
  public void jsonDeserialization() {
    TypeRefinery ref = new TypeRefinery();

    Dummy first = new Dummy("Foo", null, 12);
    Dummy second = new Dummy("Bar", first, 20);

    Gson gson = new Gson();
    String json = gson.toJson(second);

    Dummy deserialized = ref.refine(json, Dummy.class);

    assertEquals(second.label, deserialized.label);
    assertEquals(second.age, deserialized.age);
    assertEquals(first.label, deserialized.next.label);
    assertEquals(first.age, deserialized.next.age);
  }

  @Test
  public void doubleRefinement() {
    TypeRefinery ref = new TypeRefinery();
    Double browserFunctionInput = new Double(4);

    Integer int4 = ref.refine(browserFunctionInput, Integer.class);
    assertEquals(new Integer(4), int4);
    int primInt4 = ref.refine(browserFunctionInput, int.class);
    assertEquals(4, primInt4);

    Long long4 = ref.refine(browserFunctionInput, Long.class);
    assertEquals(new Long(4), long4);
    long primLong4 = ref.refine(browserFunctionInput, long.class);
    assertEquals(4l, primLong4);

    Float float4 = ref.refine(browserFunctionInput, Float.class);
    assertEquals(new Float(4), float4);
    float primFloat4 = ref.refine(browserFunctionInput, float.class);
    assertEquals(4.0f, primFloat4, 0.001);

    Double double4 = ref.refine(browserFunctionInput, Double.class);
    assertEquals(new Double(4), double4);
    double primDouble4 = ref.refine(browserFunctionInput, double.class);
    assertEquals(4.0, primDouble4, 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalDoubleToString() {
    TypeRefinery ref = new TypeRefinery();

    ref.refine(new Double(4), String.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalDoubleToBoolean() {
    TypeRefinery ref = new TypeRefinery();

    ref.refine(new Double(4), Boolean.class);
  }

  @Test
  public void booleanProcessing() {
    TypeRefinery ref = new TypeRefinery();

    Boolean refinedTrue = ref.refine(Boolean.TRUE, Boolean.class);
    Boolean refinedFalse = ref.refine(Boolean.FALSE, Boolean.class);

    assertEquals(Boolean.TRUE, refinedTrue);
    assertEquals(Boolean.FALSE, refinedFalse);
  }
}
