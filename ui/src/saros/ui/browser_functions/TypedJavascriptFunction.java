package saros.ui.browser_functions;

import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import saros.ui.JavaScriptAPI;
import saros.ui.browser_functions.BrowserFunction.Policy;
import saros.util.ThreadUtils;

/**
 * Offers type-safety at the boundary between the JavaScript and Java worlds by overwriting the
 * generic {@link #function(Object[])} method.
 *
 * <p>Subclasses need to provide the desired browser function as a standard Java method annotated
 * with {@link BrowserFunction}. This method will be called with the correctly typed and, for
 * complex types, deserialized input from the JavaScript world. Its return value (if any) will be
 * serialized, if necessary. This class ensures type-safety through reflection; furthermore, it
 * takes care of error-handling.
 *
 * <pre>
 * class MyBrowserFunction extends TypedJavascriptFunction {
 *   {@literal @}BrowserFunction
 *   public void doStuff(String label, int length) {
 *      // ...
 *   }
 * }
 * </pre>
 */
class TypedJavascriptFunction extends SelfRegisteringJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(TypedJavascriptFunction.class);

  private static final String JS_NAME_CONVENTION = "__java_{0}";

  /**
   * Retrieves the callable {@link Method} of a given TypedJavascriptFunction.
   *
   * @param clazz The typed browser function to inspect
   * @return A method of the given class that has the {@link BrowserFunction} annotation
   */
  protected static Method getCallable(Class<? extends TypedJavascriptFunction> clazz) {

    Method callable = null;
    for (Method m : clazz.getMethods()) {
      if (m.isAnnotationPresent(BrowserFunction.class)) {
        callable = m;
        break;
      }
    }

    if (callable == null) {
      throw new IllegalArgumentException(
          "class " + clazz.getName() + " has no annotated callable method");
    }

    return callable;
  }

  private final Method callable;

  /**
   * Creates a new {@link TypedJavascriptFunction} with a name following the {@linkplain
   * #JS_NAME_CONVENTION JavaScript naming convention}. This expects that the subclass has (at
   * least) one method annotated with {@link BrowserFunction} -- otherwise this will throw an {@link
   * IllegalArgumentException}.
   *
   * @param name base name of this function
   */
  public TypedJavascriptFunction(String name) {
    super(MessageFormat.format(JS_NAME_CONVENTION, name));

    callable = getCallable(this.getClass());
  }

  @Override
  public Object function(Object[] arguments) {
    final String name = callable.getName();
    final List<Object> typedArguments;

    try {
      typedArguments = getTypedArguments(arguments);

      if (callable.getAnnotation(BrowserFunction.class).value() == Policy.ASYNC) {
        invokeAsync(typedArguments);
        return null;
      } else {
        return invokeSync(typedArguments);
      }
    } catch (IllegalArgumentException e) {
      // this Exception is only for signaling the error; it has already
      // been logged before
      JavaScriptAPI.showError(browser, "Internal Error (" + name + ")");
    }

    return null;
  }

  /**
   * First, retrieve argument types through reflection. Then, refine/deserialize the inputs assuming
   * those types.
   *
   * @return List of arguments, ready to pass to {@link Method#invoke(Object, Object...)} as the
   *     second argument
   * @throws IllegalArgumentException if there is no method annotated with BrowserFunction with the
   *     right number of arguments, or if any of the arguments cannot be converted to the specified
   *     Java type
   */
  private List<Object> getTypedArguments(Object[] arguments) {
    // Determine target types by inspecting the callable
    List<Object> typedArguments = new ArrayList<Object>();
    Class<?>[] argTypes = callable.getParameterTypes();

    if (arguments.length != argTypes.length) {
      LOG.error(
          getName()
              + ": given arguments don't match signature. Given: "
              + arguments.length
              + ", expected: "
              + argTypes.length);
      throw new IllegalArgumentException();
    }

    // Go through pre-typed arguments and refine them
    TypeRefinery r = new TypeRefinery();
    for (int i = 0; i < arguments.length; i++) {
      typedArguments.add(r.refine(arguments[i], argTypes[i]));
    }

    return typedArguments;
  }

  /** Run asynchronous callables through ThreadUtils (their return value, if any, is disregarded) */
  private void invokeAsync(final List<Object> typedArguments) {
    final String name = callable.getName();

    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            try {
              callable.invoke(TypedJavascriptFunction.this, typedArguments.toArray());
            } catch (Exception e) {
              logInvocationError(e, typedArguments);
            }
          }
        };

    ThreadUtils.runSafeAsync("BrowserFunction-" + name, LOG, runnable);
  }

  /**
   * Invoke synchronous callables directly
   *
   * @return If the callable has a return type, the return value will be converted to one of the
   *     allowed JavaScript types. In particular, complex objects will serialized as JSON. If the
   *     callable is void, <code>null</code> will be returned.
   * @throws IllegalArgumentException if the invocation of the callable failed (either because of
   *     Java reflection errors, or because of internal problems with the callable itself).
   */
  private Object invokeSync(final List<Object> typedArguments) {
    // (1) Try to invoke the actual method
    Object returnedValue = null;
    try {
      returnedValue = callable.invoke(this, typedArguments.toArray());
    } catch (Exception e) {
      logInvocationError(e, typedArguments);
      throw new IllegalArgumentException(e);
    }

    // (2) Process the return value if its type is not supported by the SWT
    // Browser.
    if (returnedValue == null) return null;

    Class<?> returnType = callable.getReturnType();
    Class<?>[] allowedTypes =
        new Class<?>[] {
          Boolean.class, String.class, Integer.class, Long.class, Float.class, Double.class
        };

    if (Arrays.asList(allowedTypes).contains(returnType)) return returnedValue;

    Gson g = new Gson();
    return g.toJson(returnedValue);
  }

  private void logInvocationError(Exception e, List<Object> typedArguments) {
    String name = callable.getName();
    Class<?> clazz = e.getClass();

    if (clazz == InvocationTargetException.class) {
      LOG.error(name + " threw an exception, args: " + typedArguments, e.getCause());
    } else if (clazz == IllegalAccessException.class) {
      LOG.error(name + " cannot be accessed", e);
    } else if (clazz == IllegalArgumentException.class) {
      LOG.error(name + " was not called properly, args: " + typedArguments, e);
    }
  }
}
