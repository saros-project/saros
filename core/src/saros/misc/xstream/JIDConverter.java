package saros.misc.xstream;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import saros.net.xmpp.JID;

/** @deprecated Use {@link UserConverter} instead. */
@Deprecated
public class JIDConverter extends AbstractSingleValueConverter {

  @SuppressWarnings({"rawtypes"})
  @Override
  public boolean canConvert(Class type) {
    return type.equals(JID.class);
  }

  @Override
  public Object fromString(String str) {
    return new JID(URLCodec.decode(str));
  }

  @Override
  public String toString(Object obj) {
    return URLCodec.encode(((JID) obj).toString());
  }
}
