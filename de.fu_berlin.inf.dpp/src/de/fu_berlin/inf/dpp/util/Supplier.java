package de.fu_berlin.inf.dpp.util;

/**
 * Supplier Interface inspired by Google Collections.
 */
public interface Supplier<T> {

    /**
     * Method returning a T. No contract (like caching, thread-safety, etc.) is
     * implied with this interface.
     */
    public T get();

    /**
     * Utility class containing useful static methods on Suppliers
     */
    public static class Suppliers {

        /**
         * Returns a Supplier which will cache the result produced by the given
         * delegate supplier and return them on subsequent calls. This
         * implementation is not thread-safe. Concurrent access might result in
         * the delegate supplier being asked twice for a T.
         */
        public static <T> Supplier<T> memoize(final Supplier<T> delegate) {
            return new Supplier<T>() {
                T memoized;

                public T get() {
                    if (memoized == null) {
                        memoized = delegate.get();
                    }
                    return memoized;
                }
            };
        }

        /**
         * Returns a supplier which synchronizes on the returned supplier. This
         * implies that if all access to the delegate is achieved only through
         * the returned supplier, thread-safety is achieved.
         * 
         * Contrast with {@link #threadSafeDelegate(Supplier)}.
         */
        public static <T> Supplier<T> threadSafe(final Supplier<T> delegate) {
            return new Supplier<T>() {
                public synchronized T get() {
                    return delegate.get();
                }
            };
        }

        /**
         * Returns a supplier which synchronizes on the given delegate supplier.
         * 
         * Contrast with {@link #threadSafe(Supplier)}.
         */
        public static <T> Supplier<T> threadSafeDelegate(
            final Supplier<T> delegate) {
            return new Supplier<T>() {
                public T get() {
                    synchronized (delegate) {
                        return delegate.get();
                    }
                }
            };
        }

    }
}
