package saros.ui.browser_functions;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

/**
 * {@link BrowserFunction}s deal with a limited set of parameter and return types. This class
 * refines instances of types {@link Boolean}, {@link Double}, and {@link String} (which represent
 * JavaScript <code>boolean</code>, <code>number</code>, and <code>string</code>, respectively).
 *
 * <ul>
 *   <li>Booleans will be passed through
 *   <li>Doubles will be converted to Integers, Longs, Floats, or Doubles
 *   <li>Strings will be passed through, or, if they represent complex objects, deserizalized with
 *       {@link Gson}.
 * </ul>
 *
 * For this to work, callers need to provide the expected type, e.g. {@link Integer}, along with the
 * {@link Object} retrieved from the {@link Browser}. Example:
 *
 * <pre>
 * TypeRefinery r = new TypeRefinery();
 * <i>// arg1 being a Double upcasted as an Object</i>
 * Integer myInt = r.refine(arg1, Integer.class);
 * </pre>
 *
 * If the expected type cannot be reproduced with the given input, an {@link
 * IllegalArgumentException} will be thrown.
 *
 * @see BrowserFunction#function(Object[]) Definition of the JavaScript to Java type mapping
 */
class TypeRefinery {

  private static final Logger LOG = Logger.getLogger(TypeRefinery.class);

  private Gson gson;

  /** Create a new refinery */
  public TypeRefinery() {
    gson = new Gson();
  }

  /**
   * Try to refine the type of the given argument. The following refinements are implemented:
   *
   * <ul>
   *   <li><code>null</code> -> <code>null</code>
   *   <li>Boolean -> Boolean
   *   <li>Double -> (Integer|Long|Float|Double)
   *   <li>String -> String
   *   <li>String -> <i>serializable class</i>
   * </ul>
   *
   * For the following argument types there are <b>no</b> refinements implemented yet:
   *
   * <ul>
   *   <li>Object[] -> *
   * </ul>
   *
   * @param argument An argument coming from the JavaScript world. Can be a Boolean, a Double, a
   *     String, or <code>null</code>
   * @param targetType The type the argument should be refined to. Can be Boolean, Double, Float,
   *     Long, Integer, String, or any class that can be deserialized from JSON.
   * @return An object of type targetType, or <code>null</code> if argument was <code>null</code>
   * @throws IllegalArgumentException if the argument cannot be converted to the targetType --
   *     either because there is no refinement implemented, or the refinement attempt failed.
   */
  public <T> T refine(Object argument, Class<T> targetType) {
    if (argument instanceof Boolean) {
      return getTypedArgument((Boolean) argument, targetType);
    }
    if (argument instanceof Double) {
      return getTypedArgument((Double) argument, targetType);
    }
    if (argument instanceof String) {
      return getTypedArgument((String) argument, targetType);
    }
    if (argument == null) {
      return null;
    }

    String argTypeName = argument.getClass().getName();
    String targetTypeName = targetType.getName();
    LOG.warn(
        "Tried to refine argument of unexpected type " + argTypeName + " to " + targetTypeName);
    throw new IllegalArgumentException(
        "Cannot refine object of type " + argTypeName + " to " + targetTypeName);
  }

  /**
   * Arguments passed in as Strings were <code>string</code>s in JavaScript. They can represent
   * actual Strings or JSON objects.
   *
   * @param jsArgument As given by the browser from the JavaScript world. Needs to be a plain String
   *     or a JSON-ized object.
   * @param targetType The type the Java world actually expects
   * @return A String, if targetType is <code>String.class</code>; deserialized object otherwise.
   */
  @SuppressWarnings("unchecked")
  private <T> T getTypedArgument(String jsArgument, Class<T> targetType) {
    if (targetType.equals(String.class)) {
      return (T) jsArgument;
    }

    T javaArgument = null;
    try {
      javaArgument = gson.fromJson(jsArgument, targetType);
    } catch (JsonSyntaxException e) {
      LOG.error(
          "could not deserialize given argument "
              + jsArgument
              + " to instance of class "
              + targetType.getName());
      throw new IllegalArgumentException(e);
    }

    return javaArgument;
  }

  /**
   * Arguments passed in as Booleans were <code>boolean</code>s in JavaScript. They always represent
   * Booleans.
   *
   * @param jsArgument As given by the browser from the JavaScript world
   * @param targetType Must be <code>Boolean.class</code>
   */
  @SuppressWarnings("unchecked")
  private <T> T getTypedArgument(Boolean jsArgument, Class<T> targetType) {
    if (!targetType.equals(Boolean.class)) {
      LOG.error(
          "expected to see an instance of class "
              + targetType.getName()
              + ", but got a Boolean instead");
      throw new IllegalArgumentException();
    }

    return (T) jsArgument;
  }

  /**
   * Arguments passed in as Doubles were <code>number</code>s in JavaScript. They can represent
   * actual Doubles, Floats, Longs, or Integers, or their primitive counterparts.
   *
   * @param jsArgument As given by the browser from the JavaScript world
   * @param targetType Must be any of <code>Double.class</code>, <code>Float.class</code>, <code>
   *     Long.class</code>, or <code>Integer.class</code>, or their primitive counterparts
   * @return A Double, Float, Long, or Integer
   */
  @SuppressWarnings("unchecked")
  private <T> T getTypedArgument(Double jsArgument, Class<T> targetType) {
    if (targetType == Integer.class || targetType == Integer.TYPE) {
      return (T) Integer.valueOf(jsArgument.intValue());
    }
    if (targetType == Long.class || targetType == Long.TYPE) {
      return (T) Long.valueOf(jsArgument.longValue());
    }
    if (targetType == Float.class || targetType == Float.TYPE) {
      return (T) Float.valueOf(jsArgument.floatValue());
    }
    if (targetType == Double.class || targetType == Double.TYPE) {
      return (T) jsArgument;
    }

    LOG.error(
        "expected to see an instance of class "
            + targetType.getName()
            + ", but got a Double instead");
    throw new IllegalArgumentException();
  }
}
