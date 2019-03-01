package saros.test.mocks;

import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.picocontainer.BindKey;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.powermock.api.easymock.PowerMock;
import saros.context.IContextFactory;

/**
 * Facilitates integration tests by offering multiple ways to create a PicoContainer full of mocked
 * components.
 *
 * <p>This class does not take care of duplicate keys -- it's up to users of this class to make sure
 * no duplicates are entered in the container. "Bulk operations" with {@link
 * #addMocksFromFactory(MutablePicoContainer, IContextFactory) addMocksFromFactory()} calls should
 * be done first; use {@link #addMock(MutablePicoContainer, Class) addMock()} or {@link
 * #addMocks(MutablePicoContainer, List) addMocks()} to add missing dependencies.
 */
public class ContextMocker {

  /**
   * Creates an empty container which can be equipped with dependency mappings (e.g. "key ->
   * implementation") in various ways.
   *
   * @see #addMock(MutablePicoContainer, Class)
   * @see #addMocks(MutablePicoContainer, List)
   * @see #addMocksFromFactory(MutablePicoContainer, IContextFactory)
   * @return empty PicoContainer
   */
  public static MutablePicoContainer emptyContext() {
    PicoBuilder builder =
        new PicoBuilder(
                new CompositeInjection(new ConstructorInjection(), new AnnotatedFieldInjection()))
            .withCaching()
            .withLifecycle();

    return builder.build();
  }

  /**
   * Adds a mocked dependency to the container
   *
   * @param container target for the dependency to mock
   * @param clazz Class for which a {@linkplain PowerMock#createNiceMock(Class) nice mock} will be
   *     created. Must not add the same key twice (neither through this nor through the other add
   *     methods).
   */
  public static <T> void addMock(MutablePicoContainer container, Class<T> clazz) {

    T mock = PowerMock.createNiceMock(clazz);
    EasyMock.replay(mock);

    container.addComponent(clazz, mock);
  }

  /**
   * Creates mocks for a set of classes and adds them to the container.
   *
   * @param container target for the dependencies to mock
   * @param classes Classes for which a {@linkplain PowerMock#createNiceMock(Class) nice mock} will
   *     be created. Must not add the same key twice (neither through this nor through the other add
   *     methods).
   */
  public static void addMocks(MutablePicoContainer container, List<Class<?>> classes) {

    for (Class<?> clazz : classes) {
      addMock(container, clazz);
    }
  }

  /**
   * Reads all dependencies that a context factory defines and creates mocks for all of them.
   *
   * @param container target container for the dependencies to mock
   * @param factory Context factory from which all component dependencies (i.e. no {@link BindKey}s)
   *     are extracted to create mock instances for all of them. Must not add the same key twice
   *     (neither through this nor through the other add methods).
   */
  public static void addMocksFromFactory(MutablePicoContainer container, IContextFactory factory) {

    // prepare extraction of the dependency mapping keys from the factory
    CollectingPicoContainer collector = new CollectingPicoContainer();

    factory.createComponents(collector);

    // create mocks for all components
    for (Class<?> clazz : collector.keys) {
      addMock(container, clazz);
    }
  }

  /** Not an actual container, but it allows grabbing the entered keys */
  @SuppressWarnings("serial")
  private static class CollectingPicoContainer extends DefaultPicoContainer {
    final List<Class<?>> keys = new ArrayList<Class<?>>();

    @Override
    public MutablePicoContainer addComponent(Object key, Object value, Parameter... params) {

      // Ignore BindKey objects
      if (key instanceof Class) {
        keys.add((Class<?>) key);
      }

      return this;
    }
  }
}
