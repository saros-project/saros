package saros.lsp.context;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import saros.context.AbstractContextFactory;
import saros.repackaged.picocontainer.MutablePicoContainer;

/** ContextFactory for a single proxy. Used when a component may not be available at startup. */
public class ProxyContextFactory<T> extends AbstractContextFactory {

  private final Supplier<T> supplier;
  private final Class<T> clazz;

  public ProxyContextFactory(Class<T> clazz, Supplier<T> supplier) {
    this.clazz = clazz;
    this.supplier = supplier;
  }

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(this.clazz, this.createProxy());
  }

  public Object createProxy() {
    InvocationHandler handler =
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {

            T c = supplier.get();
            if (c != null) {
              try {
                return method.invoke(c, args);
              } catch (Exception e) {

              }
            }

            return null;
          }
        };

    return newProxyInstance(this.clazz.getClassLoader(), new Class[] {this.clazz}, handler);
  }
}
