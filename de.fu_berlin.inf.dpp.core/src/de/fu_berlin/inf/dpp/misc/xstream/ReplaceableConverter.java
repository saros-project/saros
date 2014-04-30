package de.fu_berlin.inf.dpp.misc.xstream;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A {@link ReplaceableConverter} is an XStream {@link Converter} that can be
 * exchanged for another. This is a desirable feature, because XStream only
 * allows to <em>register</em> converters but offers no built-in mechanism to
 * <em>unregister</em> them. (One can register another converter for the same
 * class to be converted, and XStream will always use the converter registered
 * most recently, but that is a memory leak.)
 */
class ReplaceableConverter implements Converter {

    private static final Logger LOG = Logger
        .getLogger(ReplaceableConverter.class);
    private Converter delegate;
    private boolean isReset;

    /**
     * Wraps any {@link Converter} so this object can be registered to XStream
     * (and stay registered), while the actual converter can be
     * {@linkplain #replace(Converter) replaced}.
     */
    public ReplaceableConverter(Converter converter) {
        if (converter == null)
            throw new IllegalArgumentException("converter must not be null");

        this.delegate = converter;
        this.isReset = false;
    }

    /**
     * Deactivates this converter.
     * <p>
     * This ensures that calls from XStream do not reach the delegate, which can
     * therefore be in any state. However, since XStream calls
     * {@link ConverterMatcher#canConvert(Class) canConvert()} lazily, the
     * delegate should expect such a call at any time.
     * 
     * @see #isReset()
     */
    public synchronized void reset() {
        this.isReset = true;
    }

    /**
     * @return <code>false</code> after construction, <code>true</code> after
     *         {@link #reset()} was called, <code>false</code> again after
     *         {@link #replace(Converter)} was called.
     */
    synchronized boolean isReset() {
        return isReset;
    }

    /**
     * Replaces the current {@link #delegate converter} with the given one. Does
     * nothing if the given converter is <code>null</code>.
     * 
     * @see #isReset()
     */
    public synchronized void replace(Converter with) {
        if (with == null)
            return;

        this.delegate = with;
        this.isReset = false;
    }

    /* Implementations of {@link Converter} */

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized boolean canConvert(Class clazz) {
        return delegate.canConvert(clazz);
    }

    @Override
    public synchronized void marshal(Object value,
        HierarchicalStreamWriter writer, MarshallingContext context) {

        if (isReset) {
            LOG.debug("Tried to marshal " + value + " with inactive converter "
                + delegate);
            return;
        }

        delegate.marshal(value, writer, context);
    }

    @Override
    public synchronized Object unmarshal(HierarchicalStreamReader reader,
        UnmarshallingContext context) {

        if (isReset) {
            LOG.debug("Tried to unmarshal with inactive converter " + delegate);
            return null;
        }

        return delegate.unmarshal(reader, context);
    }
}
