package saros.misc.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.log4j.Logger;

/**
 * A {@link ReplaceableConverter} is an XStream {@link Converter} that can be exchanged for another.
 * This is a desirable feature, because XStream only allows to <em>register</em> converters but
 * offers no built-in mechanism to <em>unregister</em> them. (One can register another converter for
 * the same class to be converted, and XStream will always use the converter registered most
 * recently, but that is a memory leak.)
 */
class ReplaceableConverter extends Replaceable<Converter> implements Converter {

  private static final Logger LOG = Logger.getLogger(ReplaceableConverter.class);

  /**
   * Wraps any {@link Converter} so this object can be registered to XStream (and stay registered),
   * while the actual converter can be {@linkplain #replace(Converter) replaced}.
   */
  public ReplaceableConverter(Converter converter) {
    super(converter);
  }

  /** Implementations of {@link Converter} */

  /**
   * Since XStream calls {@link ConverterMatcher#canConvert(Class) canConvert()} lazily, the
   * delegate should expect such a call at any time, i.e. even when {@link #isReset()} returns
   * <code>true</code>.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public synchronized boolean canConvert(Class clazz) {
    return delegate.canConvert(clazz);
  }

  @Override
  public synchronized void marshal(
      Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

    if (isReset()) {
      LOG.debug("Tried to marshal " + value + " with inactive converter " + delegate);
      return;
    }

    delegate.marshal(value, writer, context);
  }

  @Override
  public synchronized Object unmarshal(
      HierarchicalStreamReader reader, UnmarshallingContext context) {

    if (isReset()) {
      LOG.debug("Tried to unmarshal with inactive converter " + delegate);
      return null;
    }

    return delegate.unmarshal(reader, context);
  }
}
