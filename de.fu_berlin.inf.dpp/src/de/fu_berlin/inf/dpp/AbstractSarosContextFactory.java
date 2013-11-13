package de.fu_berlin.inf.dpp;

/**
 * Abstract base class that only offers syntactic sugar for handling component
 * creation.
 * 
 * @author srossbach
 */
public abstract class AbstractSarosContextFactory implements
    ISarosContextFactory {

    protected static class Component {
        private Class<?> clazz;
        private Object instance;
        private Object bindKey;

        private Component(Object bindKey, Class<?> clazz, Object instance) {
            this.bindKey = bindKey;
            this.clazz = clazz;
            this.instance = instance;
        }

        public static Component create(Object bindKey, Class<?> clazz) {
            return new Component(bindKey, clazz, null);
        }

        public static Component create(Class<?> clazz) {
            return new Component(clazz, clazz, null);
        }

        public static <T> Component create(Class<T> clazz, T instance) {
            return new Component(clazz, clazz, instance);
        }

        public Object getBindKey() {
            return bindKey;
        }

        public Object getImplementation() {
            return instance != null ? instance : clazz;
        }
    }
}
