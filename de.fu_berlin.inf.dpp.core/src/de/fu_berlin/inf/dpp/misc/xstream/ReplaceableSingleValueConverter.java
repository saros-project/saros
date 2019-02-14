package de.fu_berlin.inf.dpp.misc.xstream;

import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.apache.log4j.Logger;

/**
 * A {@link ReplaceableSingleValueConverter} is an XStream {@link SingleValueConverter} that can be
 * exchanged for another. This is a desirable feature, because XStream only allows to
 * <em>register</em> converters but offers no built-in mechanism to <em>unregister</em> them. (One
 * can register another converter for the same class to be converted, and XStream will always use
 * the converter registered most recently, but that is a memory leak.)
 */
class ReplaceableSingleValueConverter extends Replaceable<SingleValueConverter>
    implements SingleValueConverter {

  private static final Logger LOG = Logger.getLogger(ReplaceableSingleValueConverter.class);

  /**
   * Wraps any {@link SingleValueConverter} so this object can be registered to XStream (and stay
   * registered), while the actual converter can be {@linkplain #replace(SingleValueConverter)
   * replaced}.
   */
  public ReplaceableSingleValueConverter(SingleValueConverter converter) {
    super(converter);
  }

  /** Implementations of {@link SingleValueConverter} */

  /**
   * Since XStream calls {@link ConverterMatcher#canConvert(Class) canConvert()} lazily, the
   * delegate should expect such a call at any time, i.e. even when {@link #isReset()} returns
   * <code>true</code>.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public synchronized boolean canConvert(Class type) {
    return delegate.canConvert(type);
  }

  @Override
  public synchronized String toString(Object obj) {
    if (isReset()) {
      LOG.debug("Tried to marshal " + obj + " with inactive converter " + delegate);
      return "";
    }

    return delegate.toString(obj);
  }

  @Override
  public synchronized Object fromString(String str) {
    if (isReset()) {
      LOG.debug("Tried to unmarshal " + str + " with inactive converter " + delegate);
      return null;
    }

    return delegate.fromString(str);
  }
}
