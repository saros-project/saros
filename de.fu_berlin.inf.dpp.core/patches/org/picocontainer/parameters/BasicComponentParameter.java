/**
 * *************************************************************************** Copyright (C)
 * PicoContainer Organization. All rights reserved. *
 * ------------------------------------------------------------------------- * The software in this
 * package is published under the terms of the BSD * style license a copy of which has been included
 * with this distribution in * the LICENSE.txt file. * * Original code by *
 * ***************************************************************************
 */
package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.InjectInto;

/**
 * A BasicComponentParameter should be used to pass in a particular component as argument to a
 * different component's constructor. This is particularly useful in cases where several components
 * of the same type have been registered, but with a different key. Passing a ComponentParameter as
 * a parameter when registering a component will give PicoContainer a hint about what other
 * component to use in the constructor. This Parameter will never resolve against a collecting type,
 * that is not directly registered in the PicoContainer itself.
 *
 * @author Jon Tirs&eacute;n
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 * @author Thomas Heller
 */
@SuppressWarnings("serial")
public class BasicComponentParameter extends AbstractParameter implements Parameter, Serializable {

  /**
   * <code>BASIC_DEFAULT</code> is an instance of BasicComponentParameter using the default
   * constructor.
   */
  public static final BasicComponentParameter BASIC_DEFAULT = new BasicComponentParameter();

  private Object componentKey;

  /**
   * Expect a parameter matching a component of a specific key.
   *
   * @param componentKey the key of the desired addComponent
   */
  public BasicComponentParameter(Object componentKey) {
    this.componentKey = componentKey;
  }

  /** Expect any parameter of the appropriate type. */
  public BasicComponentParameter() {}

  /**
   * Check whether the given Parameter can be satisfied by the container.
   *
   * @return <code>true</code> if the Parameter can be verified.
   * @throws org.picocontainer.PicoCompositionException {@inheritDoc}
   * @see Parameter#isResolvable(PicoContainer, ComponentAdapter, Class, NameBinding ,boolean,
   *     Annotation)
   */
  public Resolver resolve(
      final PicoContainer container,
      final ComponentAdapter<?> forAdapter,
      final ComponentAdapter<?> injecteeAdapter,
      final Type expectedType,
      NameBinding expectedNameBinding,
      boolean useNames,
      Annotation binding) {

    Class<?> resolvedClassType = null;
    // TODO take this out for Pico3
    if (!(expectedType instanceof Class)) {
      if (expectedType instanceof ParameterizedType) {
        resolvedClassType = (Class<?>) ((ParameterizedType) expectedType).getRawType();
      } else {
        return new Parameter.NotResolved();
      }
    } else {
      resolvedClassType = (Class<?>) expectedType;
    }
    assert resolvedClassType != null;

    ComponentAdapter<?> componentAdapter0;
    if (injecteeAdapter == null) {
      componentAdapter0 =
          resolveAdapter(
              container, forAdapter, resolvedClassType, expectedNameBinding, useNames, binding);
    } else {
      componentAdapter0 = injecteeAdapter;
    }
    final ComponentAdapter<?> componentAdapter = componentAdapter0;
    return new Resolver() {
      public boolean isResolved() {
        return componentAdapter != null;
      }

      public Object resolveInstance() {
        if (componentAdapter == null) {
          return null;
        }
        if (componentAdapter instanceof DefaultPicoContainer.LateInstance) {
          return convert(
              getConverters(container),
              ((DefaultPicoContainer.LateInstance) componentAdapter).getComponentInstance(),
              expectedType);
          // } else if (injecteeAdapter != null && injecteeAdapter
          // instanceof DefaultPicoContainer.KnowsContainerAdapter) {
          // return
          // convert(((DefaultPicoContainer.KnowsContainerAdapter)
          // injecteeAdapter).getComponentInstance(makeInjectInto(forAdapter)),
          // expectedType);
        } else {
          return convert(
              getConverters(container),
              container.getComponent(
                  componentAdapter.getComponentKey(), makeInjectInto(forAdapter)),
              expectedType);
        }
      }

      public ComponentAdapter<?> getComponentAdapter() {
        return componentAdapter;
      }
    };
  }

  private Converters getConverters(PicoContainer container) {
    return container instanceof Converting ? ((Converting) container).getConverters() : null;
  }

  private static InjectInto makeInjectInto(ComponentAdapter<?> forAdapter) {
    return new InjectInto(forAdapter.getComponentImplementation(), forAdapter.getComponentKey());
  }

  private static Object convert(Converters converters, Object obj, Type expectedType) {
    if (obj instanceof String && expectedType != String.class) {
      obj = converters.convert((String) obj, expectedType);
    }
    return obj;
  }

  public void verify(
      PicoContainer container,
      ComponentAdapter<?> forAdapter,
      Type expectedType,
      NameBinding expectedNameBinding,
      boolean useNames,
      Annotation binding) {
    final ComponentAdapter componentAdapter =
        resolveAdapter(
            container, forAdapter, (Class<?>) expectedType, expectedNameBinding, useNames, binding);
    if (componentAdapter == null) {
      final Set<Type> set = new HashSet<Type>();
      set.add(expectedType);
      throw new AbstractInjector.UnsatisfiableDependenciesException(
          forAdapter, null, set, container);
    }
    componentAdapter.verify(container);
  }

  /**
   * Visit the current {@link Parameter}.
   *
   * @see org.picocontainer.Parameter#accept(org.picocontainer.PicoVisitor)
   */
  public void accept(final PicoVisitor visitor) {
    visitor.visitParameter(this);
  }

  protected <T> ComponentAdapter<T> resolveAdapter(
      PicoContainer container,
      ComponentAdapter adapter,
      Class<T> expectedType,
      NameBinding expectedNameBinding,
      boolean useNames,
      Annotation binding) {
    Class type = expectedType;
    if (type.isPrimitive()) {
      String expectedTypeName = expectedType.getName();
      if (expectedTypeName == "int") {
        type = Integer.class;
      } else if (expectedTypeName == "long") {
        type = Long.class;
      } else if (expectedTypeName == "float") {
        type = Float.class;
      } else if (expectedTypeName == "double") {
        type = Double.class;
      } else if (expectedTypeName == "boolean") {
        type = Boolean.class;
      } else if (expectedTypeName == "char") {
        type = Character.class;
      } else if (expectedTypeName == "short") {
        type = Short.class;
      } else if (expectedTypeName == "byte") {
        type = Byte.class;
      }
    }

    ComponentAdapter<T> result = null;
    if (componentKey != null) {
      // key tells us where to look so we follow
      result = typeComponentAdapter(container.getComponentAdapter(componentKey));
    } else if (adapter == null) {
      result = container.getComponentAdapter(type, (NameBinding) null);
    } else {
      Object excludeKey = adapter.getComponentKey();
      ComponentAdapter byKey = container.getComponentAdapter(expectedType);
      if (byKey != null && !excludeKey.equals(byKey.getComponentKey())) {
        result = typeComponentAdapter(byKey);
      }

      if (result == null && useNames) {
        ComponentAdapter found = container.getComponentAdapter(expectedNameBinding.getName());
        if ((found != null) && areCompatible(container, expectedType, found) && found != adapter) {
          result = found;
        }
      }

      if (result == null) {
        List<ComponentAdapter<T>> found =
            binding == null
                ? container.getComponentAdapters(expectedType)
                : container.getComponentAdapters(expectedType, binding.annotationType());
        removeExcludedAdapterIfApplicable(excludeKey, found);
        if (found.size() == 0) {
          result = noMatchingAdaptersFound(container, expectedType, expectedNameBinding, binding);
        } else if (found.size() == 1) {
          result = found.get(0);
        } else {
          throw tooManyMatchingAdaptersFound(expectedType, found);
        }
      }
    }

    if (result == null) {
      return null;
    }

    if (!type.isAssignableFrom(result.getComponentImplementation())) {
      // if (!(result.getComponentImplementation() == String.class &&
      // stringConverters.containsKey(type))) {
      if (!(result.getComponentImplementation() == String.class
          && getConverters(container).canConvert(type))) {
        return null;
      }
    }
    return result;
  }

  @SuppressWarnings({"unchecked"})
  private static <T> ComponentAdapter<T> typeComponentAdapter(
      ComponentAdapter<?> componentAdapter) {
    return (ComponentAdapter<T>) componentAdapter;
  }

  private <T> ComponentAdapter<T> noMatchingAdaptersFound(
      PicoContainer container,
      Class<T> expectedType,
      NameBinding expectedNameBinding,
      Annotation binding) {
    if (container.getParent() != null) {
      if (binding != null) {
        return container.getParent().getComponentAdapter(expectedType, binding.annotationType());
      } else {
        return container.getParent().getComponentAdapter(expectedType, expectedNameBinding);
      }
    } else {
      return null;
    }
  }

  private <T> AbstractInjector.AmbiguousComponentResolutionException tooManyMatchingAdaptersFound(
      Class<T> expectedType, List<ComponentAdapter<T>> found) {
    Class[] foundClasses = new Class[found.size()];
    for (int i = 0; i < foundClasses.length; i++) {
      foundClasses[i] = found.get(i).getComponentImplementation();
    }
    AbstractInjector.AmbiguousComponentResolutionException exception =
        new AbstractInjector.AmbiguousComponentResolutionException(expectedType, foundClasses);
    return exception;
  }

  private <T> void removeExcludedAdapterIfApplicable(
      Object excludeKey, List<ComponentAdapter<T>> found) {
    ComponentAdapter exclude = null;
    for (ComponentAdapter work : found) {
      if (work.getComponentKey().equals(excludeKey)) {
        exclude = work;
        break;
      }
    }
    found.remove(exclude);
  }

  private <T> boolean areCompatible(
      PicoContainer container, Class<T> expectedType, ComponentAdapter found) {
    Class foundImpl = found.getComponentImplementation();
    return expectedType.isAssignableFrom(foundImpl)
        ||
        // (foundImpl == String.class &&
        // stringConverters.containsKey(expectedType)) ;
        (foundImpl == String.class
            && getConverters(container) != null
            && getConverters(container).canConvert(expectedType));
  }
}
